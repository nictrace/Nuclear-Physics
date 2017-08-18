package org.halvors.nuclearphysics.common.utility;

import net.minecraft.util.ResourceLocation;
import org.halvors.nuclearphysics.common.Reference;
import org.halvors.nuclearphysics.common.utility.type.ResourceType;

public class ResourceUtility {
	public static ResourceLocation getResource(ResourceType resourceType, String name) {
		return new ResourceLocation(Reference.DOMAIN, resourceType.getPrefix() + name);
	}
}