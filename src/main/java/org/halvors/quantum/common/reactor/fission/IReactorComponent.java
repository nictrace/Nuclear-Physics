package org.halvors.quantum.common.reactor.fission;

import net.minecraft.item.ItemStack;

public interface IReactorComponent {
    void onReact(ItemStack itemStack, IReactor reactor);
}