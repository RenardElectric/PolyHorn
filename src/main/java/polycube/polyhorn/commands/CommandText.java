package polycube.polyhorn.commands;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

/// Shared, vanilla-client-compatible chat formatting. Never styles a caller's component in place.
public final class CommandText {
    private CommandText() {}

    private static MutableComponent colored(String text, ChatFormatting color) {
        return Component.literal(text).withStyle(color);
    }

    public static MutableComponent message() {
        return Component.empty().withStyle(ChatFormatting.GRAY)
                .append(colored("[PolyHorn] ", ChatFormatting.GOLD));
    }

    public static MutableComponent header(String title) {
        return message().append(colored(title, ChatFormatting.GOLD).withStyle(ChatFormatting.BOLD));
    }

    public static MutableComponent success(String text) {
        return message().append(colored(text, ChatFormatting.GREEN));
    }

    public static MutableComponent error(String text) {
        return error(Component.literal(text));
    }

    public static MutableComponent error(Component text) {
        return message().append(colored("Error: ", ChatFormatting.RED))
                .append(text.copy().withStyle(ChatFormatting.RED));
    }

    public static MutableComponent value(Object value) {
        return colored(String.valueOf(value), ChatFormatting.AQUA);
    }

    public static MutableComponent value(Component value) {
        return value.copy().withStyle(ChatFormatting.AQUA);
    }

    public static MutableComponent muted(String text) {
        return colored(text, ChatFormatting.DARK_GRAY);
    }

    public static MutableComponent field(String label, Component value) {
        return colored("\n  " + label + ": ", ChatFormatting.GRAY).append(value);
    }

    public static MutableComponent action(String label, String command) {
        return value(label).withStyle(style -> style.withUnderlined(true)
                .withClickEvent(new ClickEvent.SuggestCommand(command))
                .withHoverEvent(new HoverEvent.ShowText(Component.literal("Put this command in chat:\n" + command))));
    }
}
