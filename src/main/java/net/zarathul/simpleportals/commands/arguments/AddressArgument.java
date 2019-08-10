package net.zarathul.simpleportals.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.util.text.TranslationTextComponent;
import net.zarathul.simpleportals.registration.Address;

import java.util.Arrays;
import java.util.Collection;

public class AddressArgument implements ArgumentType<Address>
{
    // FIXME: make general error message for invalid address
    private static final SimpleCommandExceptionType INVALID_ADDRESS = new SimpleCommandExceptionType(new TranslationTextComponent("commands.sportals.usage"));
    // FIXME: make actual example with real block ids
    private static final Collection<String> EXAMPLES = Arrays.asList("sportals list address <addressBlockId> <addressBlockId> <addressBlockId> <addressBlockId>");

    public static AddressArgument create()
    {
        return new AddressArgument();
    }

    public static Address getValue(CommandContext<CommandSource> context, String name)
    {
        return context.getArgument(name, Address.class);
    }

    @Override
    public Address parse(StringReader reader) throws CommandSyntaxException
    {
        if (!reader.canRead()) throw INVALID_ADDRESS.createWithContext(reader);
        String[] components = new String[4];

        for (int i = 0; i < components.length; i++)
        {
            reader.skipWhitespace();
            if (!reader.canRead()) throw INVALID_ADDRESS.createWithContext(reader);
            components[i] = reader.readString();
        }

        return new Address(components[0], components[1], components[2], components[3]);
    }

    /*
    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
    {
        return null;
    }
     */

    @Override
    public Collection<String> getExamples()
    {
        return EXAMPLES;
    }
}
