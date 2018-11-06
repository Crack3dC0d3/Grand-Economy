package the_fireplace.grandeconomy.economy;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import the_fireplace.grandeconomy.GrandEconomy;
import the_fireplace.grandeconomy.Utils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class Account {
    private static HashMap<String, Account> objects = new HashMap<>();
    private static File location;
    private boolean changed;

    private UUID uuid;
    private long balance;
    private long lastLogin;
    private long lastCountActivity;

    private Account(UUID uuid) {
        this.uuid = uuid;
        this.balance = GrandEconomy.settings.getStartBalance();
        long now = Utils.getCurrentDay();
        this.lastLogin = now;
        this.lastCountActivity = now;
        this.changed = true;
    }

    public static Account get(EntityPlayer player) {
        return get(player.getUniqueID());
    }

    @Nullable
    public static Account get(UUID uuid) {
        Account account = objects.get(uuid.toString());
        if (account != null) return account;

        if (location == null) return null;
        //noinspection ResultOfMethodCallIgnored
        location.mkdirs();

        account = new Account(uuid);
        objects.put(uuid.toString(), account);

        File file = account.getFile();
        if (!file.exists()) return account;

        try {
            account.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return account;
    }

    public static void clear() {
        Account.location = null;
        Account.objects.clear();
    }

    public static void setLocation(File location) {
        Account.location = location;
    }

    public boolean update() {
        long now = Utils.getCurrentDay();
        long activityDeltaDays = now - this.lastCountActivity;
        this.lastCountActivity = now;

        if (activityDeltaDays == 0) return false;

        if (GrandEconomy.settings.isStampedMoney()) {
            if (activityDeltaDays <= GrandEconomy.settings.getResetLoginDelta()) {
                for (int i = 0; i < activityDeltaDays; i++)
                    this.balance -= Math.ceil((double) (this.balance * GrandEconomy.settings.getStampedMoneyPercent()) / 100);
            }
        }
        if (GrandEconomy.settings.isBasicIncome() && getPlayerMP() != null) {
            long loginDeltaDays = (now - this.lastLogin);
            if (loginDeltaDays > GrandEconomy.settings.getMaxLoginDelta())
                loginDeltaDays = GrandEconomy.settings.getMaxLoginDelta();
            this.lastLogin = now;
            this.balance += loginDeltaDays * GrandEconomy.settings.getBasicIncomeAmount();
        }
        if (activityDeltaDays > GrandEconomy.settings.getResetLoginDelta()) {
            this.balance = GrandEconomy.settings.getStartBalance();
        }
        return activityDeltaDays > 0;
    }

    public void writeIfChanged() throws IOException {
        if (changed) write();
    }

    private File getFile() {
        return new File(location, uuid + ".json");
    }

    private void read() throws IOException {
        read(getFile());
    }

    private void read(File file) throws IOException {
        changed = false;

        JsonParser jsonParser = new JsonParser();
        try {

            Object obj = jsonParser.parse(new FileReader(file));
            JsonObject jsonObject = (JsonObject) obj;
            balance = jsonObject.get("balance").getAsLong();
            lastLogin = jsonObject.get("lastLogin").getAsLong();
            lastCountActivity = jsonObject.get("lastCountActivity").getAsLong();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void write() throws IOException {
        write(getFile());
    }

    private void write(File location) throws IOException {
        JsonObject obj = new JsonObject();
        obj.addProperty("balance", balance);
        obj.addProperty("lastLogin", lastLogin);
        obj.addProperty("lastCountActivity", lastCountActivity);
        try (FileWriter file = new FileWriter(location)) {
            String str = obj.toString();
            file.write(str);
        }
        changed = false;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long v) {
        balance = v;
        changed = true;
        getPlayerMP().sendMessage(new TextComponentTranslation("Balance: %s", balance));
    }

    public void addBalance(long v) {
        setBalance(balance + v);
    }

    @Nullable
    private EntityPlayerMP getPlayerMP() {
        EntityPlayerMP entityPlayerMP = GrandEconomy.minecraftServer.getPlayerList().getPlayerByUUID(uuid);
        //noinspection ConstantConditions
        return (entityPlayerMP != null) ? entityPlayerMP : null;
    }
}