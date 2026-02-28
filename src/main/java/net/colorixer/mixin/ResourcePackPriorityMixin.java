package net.colorixer.mixin;

import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Mixin(ResourcePackManager.class)
public abstract class ResourcePackPriorityMixin {

    // Target the method that actually builds and saves the list, not just the getter
    @Inject(method = "buildEnabledProfiles", at = @At("RETURN"), cancellable = true)
    private void forceModResourcesToTop(Collection<String> enabledNames, CallbackInfoReturnable<List<ResourcePackProfile>> cir) {

        // The returned list is immutable, so we must copy it to a mutable ArrayList
        List<ResourcePackProfile> activeProfiles = new ArrayList<>(cir.getReturnValue());
        ResourcePackProfile myModProfile = null;

        // Hunt down your mod's auto-generated resource pack
        for (ResourcePackProfile profile : activeProfiles) {
            // Fabric usually names the pack "fabric/mod_id" or "mod/mod_id"
            if (profile.getId().contains("ttll")) {
                myModProfile = profile;
                break;
            }
        }

        // If found, rip it out and shove it at the very end of the list.
        // In Minecraft, the packs at the end of the list overwrite the ones before them!
        if (myModProfile != null) {
            activeProfiles.remove(myModProfile);
            activeProfiles.add(myModProfile);

            // Return it as an immutable list to keep the vanilla engine happy
            cir.setReturnValue(List.copyOf(activeProfiles));
        }
    }
}