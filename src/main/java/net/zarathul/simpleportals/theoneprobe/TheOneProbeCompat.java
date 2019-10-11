package net.zarathul.simpleportals.theoneprobe;

import mcjty.theoneprobe.api.ITheOneProbe;
import net.zarathul.simpleportals.SimplePortals;

import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * Hosts the callback for TheOneProbe.
 */
public final class TheOneProbeCompat implements Function<ITheOneProbe, Void>
{
	@Nullable
	@Override
	public Void apply(ITheOneProbe theOneProbe)
	{
		theOneProbe.registerProvider(new PortalInfoProvider());
		SimplePortals.log.info("TheOneProbe compatibility enabled.");

		return null;
	}
}
