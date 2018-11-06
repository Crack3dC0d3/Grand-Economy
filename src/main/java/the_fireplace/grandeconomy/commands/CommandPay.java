package the_fireplace.grandeconomy.commands;

import the_fireplace.grandeconomy.economy.Account;
import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandPay extends CommandBase {
    @Override
    @Nonnull
    public String getName() {
        return "pay";
    }

    @Override
    @Nonnull
    public String getUsage(@Nullable ICommandSender sender) {
        return "/pay <player> <amount>";
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException {
        if (args.length > 1) {
            EntityPlayerMP entityplayer = getPlayer(server, sender, args[0]);
            Account account = Account.get(entityplayer);
            account.update();
            long amount = parseLong(args[1]);
            if (amount < 0)
                //noinspection RedundantArrayCreation
                throw new NumberInvalidException("You cannot pay someone negative amount. That would be rude.", new Object[0]);
            Account senderAccount = Account.get((EntityPlayerMP) sender);
            if (senderAccount.getBalance() < amount)
                throw new InsufficientCreditException();
            senderAccount.addBalance(-amount);
            account.addBalance(amount);
            sender.sendMessage(new TextComponentTranslation("Balance: %s", senderAccount.getBalance()));
            entityplayer.sendMessage(new TextComponentTranslation("Balance: %s", account.getBalance()));
            return;
        }
        //noinspection RedundantArrayCreation
        throw new WrongUsageException("/pay <player> <amount>", new Object[0]);
    }

    @Override
    @Nonnull
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
        }
        return Collections.emptyList();
    }

    public boolean isUsernameIndex(String[] args, int index) {
        return index == 0;
    }

    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }
}