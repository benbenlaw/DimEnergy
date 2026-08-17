package com.benbenlaw.dimenergy.network;

import com.benbenlaw.dimenergy.DimEnergy;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class DimEnergyNetworking {

    public static void registerNetworking(final RegisterPayloadHandlersEvent event) {

        final PayloadRegistrar registrar = event.registrar(DimEnergy.MOD_ID);

        registrar.playToServer(UpdateDimEnergy.TYPE, UpdateDimEnergy.STREAM_CODEC, UpdateDimEnergy::handle);

        registrar.playToClient(SyncDimEnergy.TYPE, SyncDimEnergy.STREAM_CODEC, SyncDimEnergy::handle);
    }
}
