package wayoftime.bloodmagic.ritual;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.block.BlockState;
import net.minecraft.util.DamageSource;
import net.minecraftforge.fml.ModList;
import wayoftime.bloodmagic.ritual.imperfect.ImperfectRitual;
import wayoftime.bloodmagic.util.BMLog;

public class RitualManager
{
	public static final DamageSource RITUAL_DAMAGE = new DamageSource("ritual_damage").setDamageBypassesArmor();

	private final Map<String, Ritual> rituals;
	private final Map<Ritual, String> ritualsReverse;
	private final List<Ritual> sortedRituals;
	private final Map<String, ImperfectRitual> imperfectRituals;
	private final Map<ImperfectRitual, String> imperfectRitualsReverse;
//	private final Configuration config;

	public RitualManager()
	{
		this.rituals = Maps.newTreeMap();
		this.ritualsReverse = Maps.newHashMap();
		this.sortedRituals = Lists.newArrayList();
		this.imperfectRituals = Maps.newTreeMap();
		this.imperfectRitualsReverse = Maps.newHashMap();
//		this.config = config;
	}

//	public void discover(ASMDataTable dataTable)
	public void discover()
	{
		ModList.get().getAllScanData().forEach(scan -> {
			scan.getAnnotations().forEach(a -> {
				if (a.getAnnotationType().getClassName().equals(RitualRegister.class.getName()))
				{
					try
					{

						Class<?> clazz = Class.forName(a.getClassType().getClassName());
						RitualRegister ritualRegister = clazz.getAnnotation(RitualRegister.class);
						String id = ritualRegister.value();
						if (Ritual.class.isAssignableFrom(clazz))
						{
							Ritual ritual = (Ritual) clazz.newInstance();
							rituals.put(id, ritual);
							ritualsReverse.put(ritual, id);
							BMLog.DEBUG.info("Registered ritual {}", id);
						} else
						{
							BMLog.DEFAULT.error("Error creating ritual instance for {}.", id);
						}
					} catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			});
		});

		ModList.get().getAllScanData().forEach(scan -> {
			scan.getAnnotations().forEach(a -> {
				if (a.getAnnotationType().getClassName().equals(RitualRegister.Imperfect.class.getName()))
				{
					try
					{

						Class<?> clazz = Class.forName(a.getClassType().getClassName());
						RitualRegister.Imperfect ritualRegister = clazz.getAnnotation(RitualRegister.Imperfect.class);
						String id = ritualRegister.value();
						if (ImperfectRitual.class.isAssignableFrom(clazz))
						{
							ImperfectRitual ritual = (ImperfectRitual) clazz.newInstance();
							imperfectRituals.put(id, ritual);
							imperfectRitualsReverse.put(ritual, id);
							BMLog.DEBUG.info("Registered imperfect ritual {}", id);
						} else
						{
							BMLog.DEFAULT.error("Error creating imperfect ritual instance for {}.", id);
						}
					} catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			});
		});

//		syncConfig();

		// Sort rituals
		sortedRituals.addAll(rituals.values());
		// Oh dear this is probably so slow
		sortedRituals.sort((o1, o2) -> {
			Set<RitualComponent> components = Sets.newHashSet();
			o1.gatherComponents(components::add);
			int initialSize = components.size();
			components.clear();
			o2.gatherComponents(components::add);
			return Integer.compare(initialSize, components.size());
		});
	}

	public Ritual getRitual(String id)
	{
		return rituals.get(id);
	}

	public String getId(Ritual ritual)
	{
		return ritualsReverse.get(ritual);
	}

	public ImperfectRitual getImperfectRitual(BlockState state)
	{
		for (ImperfectRitual ritual : imperfectRituals.values()) if (ritual.getBlockRequirement().test(state))
			return ritual;

		return null;
	}

	public String getId(ImperfectRitual ritual)
	{
		return imperfectRitualsReverse.get(ritual);
	}

	public Collection<Ritual> getRituals()
	{
		return rituals.values();
	}

	public Collection<ImperfectRitual> getImperfectRituals()
	{
		return imperfectRituals.values();
	}

	public List<Ritual> getSortedRituals()
	{
		return sortedRituals;
	}

//	public void syncConfig()
//	{
//		config.addCustomCategoryComment("rituals", "Toggles for all rituals");
//		rituals.forEach((k, v) -> config.getBoolean(k, "rituals", true, "Enable the " + k + " ritual."));
//		imperfectRituals.forEach((k, v) -> config.getBoolean(k, "rituals.imperfect", true, "Enable the " + k + " imperfect ritual."));
//		config.save();
//	}
//
	public boolean enabled(String id, boolean imperfect)
	{
		return id != null;
//		return id != null && config.getBoolean(id, "rituals" + (imperfect ? ".imperfect" : ""), true, "");
	}
//
//	public Configuration getConfig()
//	{
//		return config;
//	}

	public static class BadRitualException extends RuntimeException
	{
		public BadRitualException(String message)
		{
			super(message);
		}
	}
}