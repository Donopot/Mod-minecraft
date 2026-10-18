package com.donopot.voiddynasty.work;

import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/** JSON envelope is encoded as one URL-safe command argument, never interpolated command text. */
public final class MissionCommand {
    private MissionCommand() {}
    private static int reply(CommandSourceStack source, JsonObject response) {
        String text = "VDMISSION " + MissionService.JSON.toJson(response);
        if (response.get("ok").getAsBoolean()) source.sendSuccess(() -> Component.literal(text), false);
        else source.sendFailure(Component.literal(text));
        return response.get("ok").getAsBoolean() ? 1 : 0;
    }
    public static LiteralArgumentBuilder<CommandSourceStack> tree() {
        return Commands.literal("mission")
            .then(Commands.literal("gather")
                .then(Commands.argument("resource",IdentifierArgument.id())
                .then(Commands.argument("amount",IntegerArgumentType.integer(1,4096))
                .then(Commands.argument("min",BlockPosArgument.blockPos())
                .then(Commands.argument("max",BlockPosArgument.blockPos())
                .then(Commands.argument("destination",BlockPosArgument.blockPos())
                .then(Commands.argument("origin",BlockPosArgument.blockPos())
                    .executes(ctx->gather(ctx,"surface"))
                    .then(Commands.literal("surface").executes(ctx->gather(ctx,"surface")))
                    .then(Commands.literal("excavate").executes(ctx->gather(ctx,"excavate"))))))))))
            .then(Commands.literal("capabilities").executes(ctx -> reply(ctx.getSource(), MissionService.capabilities())))
            .then(Commands.literal("observe").then(Commands.argument("payload",StringArgumentType.word()).executes(ctx->{
                String payload=StringArgumentType.getString(ctx,"payload");
                if(payload.length()>8192||!payload.matches("[A-Za-z0-9_-]+"))return reply(ctx.getSource(),MissionService.error("INVALID_SPEC","Invalid observation encoding"));
                try{return reply(ctx.getSource(),WorkObservation.inspect(ctx.getSource().getLevel(),JsonParser.parseString(new String(Base64.getUrlDecoder().decode(payload),StandardCharsets.UTF_8)).getAsJsonObject()));}
                catch(RuntimeException e){return reply(ctx.getSource(),MissionService.error("INVALID_SPEC","Invalid observation payload"));}
            })))
            .then(Commands.literal("estimate").then(Commands.argument("payload",StringArgumentType.word()).executes(ctx->{
                String payload=StringArgumentType.getString(ctx,"payload");
                if(payload.length()>8192 || !payload.matches("[A-Za-z0-9_-]+")) return reply(ctx.getSource(),MissionService.error("INVALID_SPEC","Invalid payload"));
                try {return reply(ctx.getSource(),MissionService.estimate(ctx.getSource().getLevel(),JsonParser.parseString(new String(Base64.getUrlDecoder().decode(payload),StandardCharsets.UTF_8)).getAsJsonObject()));}
                catch(RuntimeException e) {return reply(ctx.getSource(),MissionService.error("INVALID_SPEC","Invalid estimate payload"));}
            })))
            .then(Commands.literal("submit").then(Commands.argument("payload", StringArgumentType.word()).executes(ctx -> {
                String payload = StringArgumentType.getString(ctx, "payload");
                if (payload.length() > 8192 || !payload.matches("[A-Za-z0-9_-]+"))
                    return reply(ctx.getSource(), MissionService.error("INVALID_SPEC", "Invalid payload encoding or size"));
                try {
                    String json = new String(Base64.getUrlDecoder().decode(payload), StandardCharsets.UTF_8);
                    return reply(ctx.getSource(), MissionService.submit(ctx.getSource().getLevel(), JsonParser.parseString(json).getAsJsonObject()));
                } catch (IllegalArgumentException | IllegalStateException e) {
                    return reply(ctx.getSource(), MissionService.error("INVALID_SPEC", "Invalid JSON mission payload"));
                }
            })))
            .then(Commands.literal("status").then(Commands.argument("id", StringArgumentType.word()).executes(ctx ->
                reply(ctx.getSource(), MissionService.status(ctx.getSource().getLevel(), StringArgumentType.getString(ctx, "id"))))))
            .then(Commands.literal("control").then(Commands.argument("id", StringArgumentType.word())
                .then(Commands.argument("action", StringArgumentType.word()).executes(ctx -> reply(ctx.getSource(),
                    MissionService.control(ctx.getSource().getLevel(), StringArgumentType.getString(ctx, "id"), StringArgumentType.getString(ctx, "action")))))));
    }

    /** Human-readable solo command; identical validated mission path and permissions as MCP. */
    private static int gather(CommandContext<CommandSourceStack> ctx,String access) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        JsonObject spec=new JsonObject();spec.addProperty("schema",1);spec.addProperty("type","GATHER");
        spec.addProperty("request_id","solo-"+java.util.UUID.randomUUID());
        spec.addProperty("item",IdentifierArgument.getId(ctx,"resource").toString());
        spec.addProperty("amount",IntegerArgumentType.getInteger(ctx,"amount"));spec.addProperty("access",access);
        for(String field:java.util.List.of("min","max","destination","origin"))
            spec.add(field,MissionService.positionJson(BlockPosArgument.getBlockPos(ctx,field)));
        return reply(ctx.getSource(),MissionService.submit(ctx.getSource().getLevel(),spec));
    }
}
