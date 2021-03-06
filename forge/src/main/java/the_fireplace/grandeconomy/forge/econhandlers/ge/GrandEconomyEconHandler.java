package the_fireplace.grandeconomy.forge.econhandlers.ge;

import net.minecraftforge.common.MinecraftForge;
import the_fireplace.grandeconomy.api.GrandEconomyApi;
import the_fireplace.grandeconomy.api.IEconHandler;
import the_fireplace.grandeconomy.forge.Config;
import the_fireplace.grandeconomy.forge.GrandEconomy;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

public class GrandEconomyEconHandler implements IEconHandler {
    @Override
    public long getBalance(UUID uuid, Boolean isPlayer) {
        Account account = Account.get(uuid);
        if (account == null){
            GrandEconomy.LOGGER.warn("Account for {} was null", uuid.toString());
            return 0;
        }
        account.update();
        return account.getBalance();
    }

    @Override
    public boolean addToBalance(UUID uuid, long amount, Boolean isPlayer) {
        Account account = Account.get(uuid);
        if(account == null) {
            GrandEconomy.LOGGER.warn("Account for {} was null", uuid.toString());
            return false;
        }
        if(account.getBalance() + amount < 0)
            return false;
        account.addBalance(amount);
        return true;
    }

    @Override
    public boolean takeFromBalance(UUID uuid, long amount, Boolean isPlayer) {
        Account account = Account.get(uuid);
        if (account == null){
            GrandEconomy.LOGGER.warn("Account for {} was null", uuid.toString());
            return false;
        }
        if (account.getBalance() < amount)
            return false;
        account.addBalance(-amount);
        return true;
    }

    @Override
    public boolean setBalance(UUID uuid, long amount, Boolean isPlayer) {
        Account account = Account.get(uuid);
        if (account == null){
            GrandEconomy.LOGGER.warn("Account for {} was null", uuid.toString());
            return false;
        }
        if(amount < 0)
            return false;

        account.setBalance(amount);
        return true;
    }

    @Override
    public String getCurrencyName(long amount) {
        if (amount == 1)
            return Config.currencyNameSingular;
        return Config.currencyNameMultiple;
    }

    @Override
    public String getFormattedCurrency(long amount) {
        return amount + " " + getCurrencyName(amount);
    }

    @Override
    public boolean ensureAccountExists(UUID uuid, Boolean isPlayer) {
        return Account.get(uuid) != null;
    }

    @Override
    public Boolean forceSave(UUID uuid, Boolean isPlayer) {
        try {
            Objects.requireNonNull(Account.get(uuid)).writeIfChanged();
            return true;
        } catch(IOException e) {
            return false;
        }
    }

    @Override
    public String getId() {
        return GrandEconomyApi.MODID;
    }

    @Override
    public void init() {
        MinecraftForge.EVENT_BUS.register(new EventHandler());
    }
}
