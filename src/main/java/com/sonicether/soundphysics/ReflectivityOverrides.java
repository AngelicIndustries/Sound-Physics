package com.sonicether.soundphysics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;

/**
 * Per-block reflectivity from the config's "Block Reflectivity Overrides", for blocks whose step sound says the wrong
 * thing, such as Dimensional Doors' Altered Fabric, which sounds like stone underfoot but should soak up sound like
 * wool. An entry reads modid:name[:metadata]=value, the value a material's name (stone, wood, ground, foliage, metal,
 * glass, cloth, sand, snow) or a number from 0 to 1. Blocks are looked up when sounds are first traced, since the
 * config loads before they exist.
 */
public final class ReflectivityOverrides {

	private static final class Rule {
		final String block;
		final int metadata;
		final String value;

		Rule(String block, int metadata, String value) {
			this.block = block;
			this.metadata = metadata;
			this.value = value;
		}
	}

	private static List<Rule> rules = new ArrayList<Rule>();
	/** Per block, the reflectivity for each metadata (index 16 for any), or NaN where there is none. */
	private static Map<Block, float[]> resolved;

	private ReflectivityOverrides() {}

	/** Reads the config entries; ones that don't parse are logged and left out. */
	public static void load(String[] entries) {
		List<Rule> parsed = new ArrayList<Rule>();
		for (String entry : entries) {
			int equals = entry.lastIndexOf('=');
			String[] name = equals < 0 ? new String[0] : entry.substring(0, equals).trim().split(":");
			if (name.length < 2 || name.length > 3) {
				SoundPhysics.log("Ignoring block reflectivity override '" + entry + "'");
				continue;
			}
			int metadata = -1;
			if (name.length == 3) {
				try {
					metadata = Integer.parseInt(name[2].trim());
				} catch (NumberFormatException e) {
					SoundPhysics.log("Ignoring block reflectivity override '" + entry + "'");
					continue;
				}
			}
			parsed.add(new Rule(name[0].trim() + ":" + name[1].trim(), metadata,
					entry.substring(equals + 1).trim().toLowerCase()));
		}
		rules = parsed;
		resolved = null;
	}

	/** The overriding reflectivity of a block with this metadata, before the global reflectance, or NaN. */
	public static float get(Block block, int metadata) {
		if (rules.isEmpty()) return Float.NaN;
		if (resolved == null) resolve();
		float[] values = resolved.get(block);
		if (values == null) return Float.NaN;
		float value = values[metadata & 15];
		return Float.isNaN(value) ? values[16] : value;
	}

	private static void resolve() {
		Map<Block, float[]> map = new HashMap<Block, float[]>();
		for (Rule rule : rules) {
			Object found = Block.blockRegistry.getObject(rule.block);
			float value = value(rule.value);
			if (!(found instanceof Block) || Float.isNaN(value)) {
				SoundPhysics.log("Block reflectivity override for " + rule.block + " names an unknown block or value");
				continue;
			}
			float[] values = map.get(found);
			if (values == null) {
				values = new float[17];
				java.util.Arrays.fill(values, Float.NaN);
				map.put((Block) found, values);
			}
			values[rule.metadata < 0 ? 16 : rule.metadata & 15] = value;
		}
		resolved = map;
	}

	private static float value(String value) {
		if (value.equals("stone")) return Config.stoneReflectivity;
		if (value.equals("wood")) return Config.woodReflectivity;
		if (value.equals("ground")) return Config.groundReflectivity;
		if (value.equals("foliage")) return Config.plantReflectivity;
		if (value.equals("metal")) return Config.metalReflectivity;
		if (value.equals("glass")) return Config.glassReflectivity;
		if (value.equals("cloth")) return Config.clothReflectivity;
		if (value.equals("sand")) return Config.sandReflectivity;
		if (value.equals("snow")) return Config.snowReflectivity;
		try {
			float number = Float.parseFloat(value);
			return number >= 0 && number <= 1 ? number : Float.NaN;
		} catch (NumberFormatException e) {
			return Float.NaN;
		}
	}
}
