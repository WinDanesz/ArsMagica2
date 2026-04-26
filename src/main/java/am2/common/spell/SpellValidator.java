package am2.common.spell;

import am2.api.SpellRegistryHelper;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellModifier;
import am2.api.spell.SpellPart;
import am2.api.spell.SpellShape;
import am2.common.spell.component.Summon;
import net.minecraft.util.text.translation.I18n;

import java.util.ArrayList;

public class SpellValidator {
    public static final SpellValidator instance = new SpellValidator();

    private enum StageValidations {
        VALID,
        TERMINUS,
        PRINCIPUM,
        NOT_VALID
    }

    private SpellValidator() {

    }

    public ValidationResult spellDefIsValid(ArrayList<ArrayList<SpellPart>> shapeGroups, ArrayList<ArrayList<SpellPart>> segmented) {
        boolean noParts = true;
        for (int i = 0; i < shapeGroups.size(); ++i)
            if (!shapeGroups.get(i).isEmpty())
                noParts = false;
        for (int i = 0; i < segmented.size(); ++i)
            if (!segmented.get(i).isEmpty())
                noParts = false;
        if (noParts) return new ValidationResult(null, "");

        boolean validatedAny = false;
        for (int x = 0; x < shapeGroups.size(); ++x) {
            if (!shapeGroups.get(x).isEmpty()) {
                if (segmented.isEmpty()) {
                    ValidationResult result = internalValidation(splitToStages(shapeGroups.get(x)));
                    if (result != null)
                        return result;
                }
                ArrayList<SpellPart> concatenated = new ArrayList<SpellPart>();
                concatenated.addAll(shapeGroups.get(x));
                for (int i = 0; i < segmented.size(); ++i) {
                    concatenated.addAll(segmented.get(i));
                }
                ValidationResult result = internalValidation(splitToStages(concatenated));
                if (result != null)
                    return result;

                validatedAny = true;
            }
        }

        if (!validatedAny) {
            ValidationResult result = internalValidation(segmented);
            if (result != null)
                return result;
        }

        return new ValidationResult();
    }

    private ValidationResult internalValidation(ArrayList<ArrayList<SpellPart>> segmented) {
        for (int i = 0; i < segmented.size(); ++i) {
            StageValidations result = validateStage(segmented.get(i), i == segmented.size() - 1);

            if (result == StageValidations.NOT_VALID) {
                // Fix client crash on spell cast: Add bounds checking for spell validation
                if (i < segmented.size() && !segmented.get(i).isEmpty()) {
                    return new ValidationResult(segmented.get(i).get(0), I18n.translateToLocal("am2.spell.validate.compMiss"));
                }
            } else if (result == StageValidations.PRINCIPUM && i == segmented.size() - 1) {
                // A shape that is BOTH principum and terminus (e.g. Glyph detonator) is valid standing alone
                boolean selfContained = false;
                for (SpellPart p : segmented.get(i)) {
                    if (p instanceof SpellShape && ((SpellShape) p).isTerminusShape()) { selfContained = true; break; }
                }
                if (!selfContained && i < segmented.size() && !segmented.get(i).isEmpty()) {
                    return new ValidationResult(segmented.get(i).get(0), String.format("%s %s", SpellRegistryHelper.getSkillFromPart(segmented.get(i).get(0)).getName(), I18n.translateToLocal("am2.spell.validate.principum")));
                }
            } else if (result == StageValidations.TERMINUS && i < segmented.size() - 1) {
                if (i < segmented.size() && !segmented.get(i).isEmpty()) {
                    return new ValidationResult(segmented.get(i).get(0), String.format("%s %s", SpellRegistryHelper.getSkillFromPart(segmented.get(i).get(0)).getName(), I18n.translateToLocal("am2.spell.validate.terminus")));
                }
            }
        }

        return null;
    }

    private StageValidations validateStage(ArrayList<SpellPart> stageDefinition, boolean isFinalStage) {
        boolean terminus = false;
        boolean principum = false;
        boolean one_component = !isFinalStage;
        boolean one_shape = false;
        for (SpellPart part : stageDefinition) {
            if (part instanceof Summon) return StageValidations.TERMINUS;
            if (part instanceof SpellShape) {
                one_shape = true;
                if (((SpellShape) part).isTerminusShape())
                    terminus = true;
                if (((SpellShape) part).isPrincipumShape())
                    principum = true;
                continue;
            }
            if (part instanceof SpellComponent) {
                one_component = true;
                continue;
            }
        }

        if (principum)
            return StageValidations.PRINCIPUM;
        if (!one_component || !one_shape)
            return StageValidations.NOT_VALID;
        if (terminus)
            return StageValidations.TERMINUS;
        return StageValidations.VALID;
    }

    public static ArrayList<ArrayList<SpellPart>> splitToStages(ArrayList<SpellPart> currentRecipe) {
        ArrayList<ArrayList<SpellPart>> segmented = new ArrayList<ArrayList<SpellPart>>();
        int idx = (!currentRecipe.isEmpty() && currentRecipe.get(0) instanceof SpellShape) ? -1 : 0;
        for (int i = 0; i < currentRecipe.size(); ++i) {
            SpellPart part = currentRecipe.get(i);
            if (part instanceof SpellShape)
                idx++;
            if (segmented.size() - 1 < idx) //while loop not necessary as this will keep up
                segmented.add(new ArrayList<SpellPart>());
            segmented.get(idx).add(part);
        }
        return segmented;
    }

    public boolean modifierCanBeAdded(SpellModifier modifier) {
        return false;
    }

    public class ValidationResult {
        public final boolean valid;
        public final SpellPart offendingPart;
        public final String message;

        public ValidationResult(SpellPart offendingPart, String message) {
            valid = false;
            this.offendingPart = offendingPart;
            this.message = message;
        }

        public ValidationResult() {
            valid = true;
            this.offendingPart = null;
            this.message = "";
        }
    }
}
