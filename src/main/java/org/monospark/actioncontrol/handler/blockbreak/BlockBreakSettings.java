package org.monospark.actioncontrol.handler.blockbreak;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.monospark.actioncontrol.handler.ActionResponse;
import org.monospark.actioncontrol.handler.ActionSettings;
import org.monospark.actioncontrol.kind.block.BlockKind;
import org.monospark.actioncontrol.kind.block.BlockKindMatcher;
import org.spongepowered.api.block.BlockState;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public final class BlockBreakSettings extends ActionSettings {

	private Set<BlockKindMatcher> matchers;
	
	BlockBreakSettings(ActionResponse response, Set<BlockKindMatcher> matchers) {
		super(response);
		this.matchers = matchers;
	}
	
	public boolean canBreak(BlockState state) {
		for(BlockKindMatcher matcher : matchers) {
			if(matcher.matchesBlockState(state)) {
				return getResponse() == ActionResponse.ALLOW;
			}
		}
		return getResponse() == ActionResponse.DENY;
	}

	Set<BlockKindMatcher> getMatchers() {
		return matchers;
	}

	static final class Deserializer implements JsonDeserializer<BlockBreakSettings> {
		
		Deserializer() {}
		
		@Override
		public BlockBreakSettings deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			JsonObject object = json.getAsJsonObject();
			ActionResponse response = context.deserialize(object.get("response"), ActionResponse.class);
			String[] ids = context.deserialize(object.get("ids"), String[].class);
			Set<BlockKindMatcher> matchers = new HashSet<BlockKindMatcher>();
			for(String id : ids) {
				Optional<BlockKindMatcher> matcher = BlockKind.getRegistry().get(id);
				if(!matcher.isPresent()) {
					throw new JsonParseException("Invalid id: " + id);
				}
				matchers.add(matcher.get());
			}
			return new BlockBreakSettings(response, matchers);
		}
	}
}
