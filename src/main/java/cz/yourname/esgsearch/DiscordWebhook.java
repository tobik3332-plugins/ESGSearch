package cz.yourname.esgsearch;

import org.bukkit.Bukkit;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DiscordWebhook {
    public static void sendLog(String content) {
        if (!ESGSearch.getInstance().getConfig().getBoolean("discord-webhook.enabled", false)) return;
        String webhookUrl = ESGSearch.getInstance().getConfig().getString("discord-webhook.url", "");
        if (webhookUrl.isEmpty() || webhookUrl.contains("YOUR_WEBHOOK_URL_HERE")) return;

        Bukkit.getScheduler().runTaskAsynchronously(ESGSearch.getInstance(), () -> {
            try {
                URL url = new URL(webhookUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                connection.setDoOutput(true);

                String jsonPayload = "{\"content\": \"" + content.replace("\\", "\\\\").replace("\"", "\\\"") + "\"}";

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
                connection.getResponseCode();
                connection.disconnect();
            } catch (Exception e) {
                ESGSearch.getInstance().getLogger().warning("Webhook error: " + e.getMessage());
            }
        });
    }
}
