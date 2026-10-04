package am2.api.blocks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Position-by-position comparison of an {@link IMultiblock} against the world, used to tell players
 * what is still missing from a structure that does not {@link IMultiblock#matches match}.
 * <p>
 * Groups that already match count as fully satisfied. For alternative groups (the ones added together
 * through {@link IMultiblock#addGroup}) the closest alternative is diagnosed; for
 * {@link TypedMultiblockGroup}s the material variant that best fits the blocks already placed is assumed.
 */
public final class MultiblockDiagnosis {

    /** Supplies extra material variants for a typed group, e.g. ones deduced from oredict blocks found in the world. */
    @FunctionalInterface
    public interface VariantSupplier {
        List<Map<Integer, IBlockState>> getExtraVariants(TypedMultiblockGroup group, World world, BlockPos origin);
    }

    public static final class Problem {
        /** Offset from the structure origin. */
        public final BlockPos offset;
        /** The state that belongs here, or null if it can't be deduced (e.g. no material of a typed group placed yet). */
        @Nullable
        public final IBlockState expected;
        /** True if the right block is present but its facing/half/variant state is wrong. */
        public final boolean wrongState;
        public final String groupName;
        /** The type id within a {@link TypedMultiblockGroup}, 0 for plain groups. */
        public final int typeId;

        Problem(BlockPos offset, @Nullable IBlockState expected, boolean wrongState, String groupName, int typeId) {
            this.offset = offset;
            this.expected = expected;
            this.wrongState = wrongState;
            this.groupName = groupName;
            this.typeId = typeId;
        }
    }

    private final IMultiblock multiblock;
    private final List<Problem> problems;
    private final int satisfied;
    private final int total;

    private MultiblockDiagnosis(IMultiblock multiblock, List<Problem> problems, int satisfied, int total) {
        this.multiblock = multiblock;
        this.problems = Collections.unmodifiableList(problems);
        this.satisfied = satisfied;
        this.total = total;
    }

    public IMultiblock getMultiblock() {
        return multiblock;
    }

    public List<Problem> getProblems() {
        return problems;
    }

    /** Number of positions that already hold the correct block. */
    public int getSatisfied() {
        return satisfied;
    }

    public int getTotal() {
        return total;
    }

    public float getCompletion() {
        return total == 0 ? 1 : (float) satisfied / total;
    }

    public static MultiblockDiagnosis diagnose(IMultiblock multiblock, World world, BlockPos origin, @Nullable VariantSupplier extraVariants) {
        List<Problem> problems = new ArrayList<>();
        int satisfied = 0;
        int total = 0;
        for (List<IMultiblockGroup> alternatives : multiblock.getMultiblockGroups()) {
            GroupResult best = null;
            for (IMultiblockGroup group : alternatives) {
                GroupResult result = diagnoseGroup(group, world, origin, extraVariants);
                if (best == null || result.isBetterThan(best))
                    best = result;
                if (result.problems.isEmpty())
                    break;
            }
            if (best == null) continue;
            problems.addAll(best.problems);
            satisfied += best.satisfied;
            total += best.satisfied + best.problems.size();
        }
        return new MultiblockDiagnosis(multiblock, problems, satisfied, total);
    }

    /**
     * Diagnoses each candidate (e.g. the possible orientations of a structure) and returns the most complete one.
     */
    public static MultiblockDiagnosis diagnoseBest(World world, BlockPos origin, @Nullable VariantSupplier extraVariants, IMultiblock... candidates) {
        MultiblockDiagnosis best = null;
        for (IMultiblock candidate : candidates) {
            MultiblockDiagnosis diagnosis = diagnose(candidate, world, origin, extraVariants);
            if (best == null || diagnosis.getCompletion() > best.getCompletion())
                best = diagnosis;
        }
        return best;
    }

    private static GroupResult diagnoseGroup(IMultiblockGroup group, World world, BlockPos origin, @Nullable VariantSupplier extraVariants) {
        String name = group instanceof MultiblockGroup ? ((MultiblockGroup) group).getName() : "";
        if (group.matches(world, origin))
            return new GroupResult(group.getPositions().size(), new ArrayList<>());

        if (group instanceof TypedMultiblockGroup) {
            TypedMultiblockGroup typed = (TypedMultiblockGroup) group;
            List<Map<Integer, IBlockState>> variants = new ArrayList<>(typed.getVariants());
            if (extraVariants != null)
                variants.addAll(extraVariants.getExtraVariants(typed, world, origin));

            GroupResult best = null;
            for (Map<Integer, IBlockState> variant : variants) {
                GroupResult result = diagnoseTypedVariant(typed, variant, world, origin);
                if (best == null || result.isBetterThan(best))
                    best = result;
            }
            if (best == null || best.score() == 0) {
                // Nothing of this group placed yet: the material can't be deduced
                List<Problem> problems = new ArrayList<>();
                for (BlockPos pos : typed.getPositions())
                    problems.add(new Problem(pos, null, false, name, typed.getGroup(pos)));
                return new GroupResult(0, problems);
            }
            return best;
        }

        boolean ignoreState = group instanceof MultiblockGroup && ((MultiblockGroup) group).isIgnoreState();
        List<IBlockState> accepted = group.getStates();
        int satisfied = 0;
        List<Problem> problems = new ArrayList<>();
        for (BlockPos pos : group.getPositions()) {
            IBlockState actual = world.getBlockState(origin.add(pos));
            boolean match = false;
            boolean sameBlock = false;
            for (IBlockState state : accepted) {
                if (actual.getBlock() == state.getBlock()) {
                    sameBlock = true;
                    if (ignoreState || actual.equals(state)) {
                        match = true;
                        break;
                    }
                }
            }
            if (match)
                satisfied++;
            else
                problems.add(new Problem(pos, accepted.isEmpty() ? null : accepted.get(0), sameBlock, name, 0));
        }
        return new GroupResult(satisfied, problems);
    }

    private static GroupResult diagnoseTypedVariant(TypedMultiblockGroup group, Map<Integer, IBlockState> variant, World world, BlockPos origin) {
        int satisfied = 0;
        List<Problem> problems = new ArrayList<>();
        for (BlockPos pos : group.getPositions()) {
            int typeId = group.getGroup(pos);
            IBlockState expected = variant.get(typeId);
            IBlockState actual = world.getBlockState(origin.add(pos));
            boolean sameBlock = expected != null && actual.getBlock() == expected.getBlock();
            // Same comparison as TypedMultiblockGroup#matches
            if (sameBlock && (group.isIgnoreState() || actual.getBlock().getMetaFromState(actual) == expected.getBlock().getMetaFromState(expected)))
                satisfied++;
            else
                problems.add(new Problem(pos, expected, sameBlock, group.getName(), typeId));
        }
        return new GroupResult(satisfied, problems);
    }

    private static final class GroupResult {
        final int satisfied;
        final List<Problem> problems;

        GroupResult(int satisfied, List<Problem> problems) {
            this.satisfied = satisfied;
            this.problems = problems;
        }

        /** Correct blocks count fully, right block with wrong facing counts half. */
        int score() {
            int wrongState = 0;
            for (Problem problem : problems)
                if (problem.wrongState) wrongState++;
            return satisfied * 2 + wrongState;
        }

        boolean isBetterThan(GroupResult other) {
            return score() > other.score();
        }
    }
}
