package gruvexp.bbminigames.twtClassic.ability.abilities;

import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.ability.Ability;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;

public class EnderPearlAbility extends Ability {
    public EnderPearlAbility(BotBowsPlayer bp, int slot) {
        super(bp, slot);
        this.type = AbilityType.ENDER_PEARL;
        this.baseCooldown = type.getBaseCooldown();
    }
}
