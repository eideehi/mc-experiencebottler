/*
 * MIT License
 *
 * Copyright (c) 2022 EideeHi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.eidee.minecraft.experiencebottler.core.i18n;

import static net.eidee.minecraft.experiencebottler.ExperienceBottlerMod.getLogger;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

/** This class is used to download and get translations from Crowdin. */
@Environment(EnvType.CLIENT)
public class CrowdinTranslations {
  private static final long CONNECTION_INTERVAL;
  private static final BiMap<String, String> MINECRAFT_CROWDIN_LANGUAGE_CODE;

  static {
    CONNECTION_INTERVAL = 1000 * 60 * 60 * 24;

    Map<String, String> map = Maps.newHashMap();

    map.put("af_za", "af");
    map.put("ar_sa", "ar");
    map.put("ast_es", "ast");
    map.put("az_az", "az");
    map.put("ba_ru", "ba");
    // Bavarian: bar
    map.put("be_by", "be");
    map.put("bg_bg", "bg");
    map.put("br_fr", "br-FR");
    // Brabantian: brb
    map.put("bs_ba", "bs");
    map.put("ca_es", "ca");
    map.put("cs_cz", "cs");
    map.put("cy_gb", "cy");
    map.put("da_dk", "da");
    map.put("de_at", "de-AT");
    map.put("de_ch", "de-CH");
    map.put("de_de", "de");
    map.put("el_gr", "el");
    map.put("en_au", "en-AU");
    map.put("en_ca", "en-CA");
    map.put("en_gb", "en-GB");
    map.put("en_nz", "en-NZ");
    map.put("en_pt", "en-PT");
    map.put("en_ud", "en-UD");
    //map.put("en_us", "en-US");
    // Modern English minus borrowed words[1]: enp
    // Early Modern English: enws
    map.put("eo_uy", "eo");
    map.put("es_ar", "es-AR");
    map.put("es_cl", "es-CL");
    map.put("es_ec", "es-EC");
    map.put("es_es", "es-ES");
    map.put("es_mx", "es-MX");
    map.put("es_uy", "es-UY");
    map.put("es_ve", "es-VE");
    // Andalusian: esan
    map.put("et_ee", "et");
    map.put("eu_es", "eu");
    map.put("fa_ir", "fa");
    map.put("fi_fi", "fi");
    map.put("fil_ph", "fil");
    map.put("fo_fo", "fo");
    map.put("fr_ca", "fr-CA");
    map.put("fr_fr", "fr");
    map.put("fra_de", "fra-DE");
    map.put("fur_it", "fur-IT");
    map.put("fy_nl", "fy-NL");
    map.put("ga_ie", "ga-IE");
    map.put("gd_gb", "gd");
    map.put("gl_es", "gl");
    map.put("haw_us", "haw");
    map.put("he_il", "he");
    map.put("hi_in", "hi");
    map.put("hr_hr", "hr");
    map.put("hu_hu", "hu");
    map.put("hy_am", "hy-AM");
    map.put("id_id", "id");
    map.put("ig_ng", "ig");
    map.put("io_en", "ido");
    map.put("is_is", "is");
    // Interslavic: isv
    map.put("it_it", "it");
    map.put("ja_jp", "ja");
    map.put("jbo_en", "jbo");
    map.put("ka_ge", "ka");
    map.put("kk_kz", "kk");
    map.put("kn_in", "kn");
    map.put("ko_kr", "ko");
    // Kölsch/Ripuarian: ksh
    map.put("kw_gb", "kw");
    map.put("la_la", "la-LA");
    map.put("lb_lu", "lb");
    map.put("li_li", "li");
    // Lombard: lmo
    map.put("lol_us", "lol");
    map.put("lt_lt", "lt");
    map.put("lv_lv", "lv");
    // Classical Chinese: lzh
    map.put("mk_mk", "mk");
    map.put("mn_mn", "mn");
    map.put("ms_my", "ms");
    map.put("mt_mt", "mt");
    map.put("nds_de", "nds");
    map.put("nl_be", "nl-BE");
    map.put("nl_nl", "nl");
    map.put("nn_no", "nn-NO");
    map.put("no_no", "nb");
    map.put("oc_fr", "oc");
    // Elfdalian: ovd
    map.put("pl_pl", "pl");
    map.put("pt_br", "pt-BR");
    map.put("pt_pt", "pt-PT");
    map.put("qya_aa", "qya-AA");
    map.put("ro_ro", "ro");
    // Russian (Pre-revolutionary): rpr
    map.put("ru_ru", "ru");
    map.put("se_no", "se");
    map.put("sk_sk", "sk");
    map.put("sl_si", "sl");
    map.put("so_so", "so");
    map.put("sq_al", "sq");
    map.put("sr_sp", "sr");
    map.put("sv_se", "sv-SE");
    // Upper Saxon German: sxu
    // Silesian: szl
    map.put("ta_in", "ta");
    map.put("th_th", "th");
    map.put("tl_ph", "tl");
    map.put("tlh_aa", "tlh-AA");
    // Toki Pona: tok
    map.put("tr_tr", "tr");
    map.put("tt_ru", "tt-RU");
    map.put("uk_ua", "uk");
    map.put("val_es", "val-ES");
    map.put("vec_it", "vec");
    map.put("vi_vn", "vi");
    map.put("yi_de", "yi");
    map.put("yo_ng", "yo");
    map.put("zh_cn", "zh-CN");
    map.put("zh_hk", "zh-HK");
    map.put("zh_tw", "zh-TW");

    MINECRAFT_CROWDIN_LANGUAGE_CODE = ImmutableBiMap.copyOf(map);
  }

  private final File translationsDir;
  private final String modId;
  private final String crowdinProjectId;

  private Path timestampFile;
  private Timestamp timestamp;

  public CrowdinTranslations(String modId, String crowdinProjectId) {
    this.translationsDir = new File(MinecraftClient.getInstance().runDirectory, "ModTranslations");
    this.modId = modId;
    this.crowdinProjectId = crowdinProjectId;
  }

  private static String toMinecraftLanguageCode(String crowdinLanguageCode) {
    return MINECRAFT_CROWDIN_LANGUAGE_CODE.inverse().getOrDefault(crowdinLanguageCode, "");
  }

  private static String toCrowinLanguageCode(String minecraftLanguageCode) {
    return MINECRAFT_CROWDIN_LANGUAGE_CODE.getOrDefault(minecraftLanguageCode, "");
  }

  public static CrowdinTranslations newInstance(String modId, String crowdinProjectId) {
    return new CrowdinTranslations(modId, crowdinProjectId);
  }

  private Path getTimestampFilePath() throws IOException {
    if (timestampFile == null) {
      timestampFile = Paths.get(translationsDir.getCanonicalPath(), modId + ".timestamp");
    }
    return timestampFile;
  }

  private Timestamp getTimestamp() throws IOException {
    if (timestamp == null) {
      Path path = getTimestampFilePath();

      if (Files.isRegularFile(path)) {
        try (Reader reader = Files.newBufferedReader(path)) {
          timestamp = (new Gson()).fromJson(reader, Timestamp.class);
        } catch (JsonSyntaxException e) {
          getLogger().warn("Error parsing timestamp file: " + path, e);
        }
      }

      if (timestamp == null) {
        timestamp = new Timestamp();
      }
    }
    return timestamp;
  }

  private URL getTranslationsUrl(String crowdinLanguageCode) throws MalformedURLException {
    return new URL(String.format("https://crowdin.com/backend/download/project/%s/%s.zip", crowdinProjectId, crowdinLanguageCode));
  }

  private Path getTranslationsPath(String minecraftLanguageCode) throws IOException {
    return Paths.get(translationsDir.getCanonicalPath(), "assets", modId, "lang", minecraftLanguageCode + ".json");
  }

  private boolean checkDownloadNeeds(String crowdinLanguageCode) throws IOException {
    Path path = getTimestampFilePath();
    if (Files.notExists(path) || !Files.isRegularFile(path)) {
      return true;
    }

    Timestamp timestamp = getTimestamp();
    long lastConnection = timestamp.getLastConnection(crowdinLanguageCode);
    long lastModified = timestamp.getLastModified(crowdinLanguageCode);

    long now = Instant.now().toEpochMilli();
    if (lastConnection + CONNECTION_INTERVAL > now) {
      return false;
    }

    URL url = getTranslationsUrl(crowdinLanguageCode);
    if (url.openConnection() instanceof HttpURLConnection connection) {
      timestamp.setLastConnection(crowdinLanguageCode, now);
      if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
        return false;
      }
      return connection.getLastModified() > lastModified;
    }

    return false;
  }

  private void tryDownload(String crowdinLanguageCode) throws IOException {
    String minecraftLanguageCode = toMinecraftLanguageCode(crowdinLanguageCode);

    Path outputPath = getTranslationsPath(minecraftLanguageCode);

    Path parent = outputPath.getParent();
    if (!Files.exists(parent)) {
      Files.createDirectories(parent);
    }

    URL url = getTranslationsUrl(crowdinLanguageCode);

    getLogger().debug("Try downloading " + url);

    if (url.openConnection() instanceof HttpURLConnection connection) {
      long lastModified = connection.getLastModified();

      ZipInputStream zip = new ZipInputStream(connection.getInputStream());
      byte[] buffer = new byte[1024];

      try (OutputStream outputStream = Files.newOutputStream(outputPath)) {
        ZipEntry entry;
        while ((entry = zip.getNextEntry()) != null) {
          if (entry.getName().equals(modId + ".json")) {
            int read;
            while ((read = zip.read(buffer)) != -1) {
              outputStream.write(buffer, 0, read);
            }
            break;
          }
        }
      }

      getTimestamp().setLastConnection(crowdinLanguageCode, Instant.now().toEpochMilli());
      getTimestamp().setLastModified(crowdinLanguageCode, lastModified);
      connection.disconnect();
    }
  }

  /**
   * Get the translations for the given language code.
   * Translations are downloaded from Crowdin if needed.
   *
   * @param minecraftLanguageCode The language code of the translation to be obtained.
   * @return The translations for the given language code.
   */
  public InputStream getAsStream(String minecraftLanguageCode) {
    String crowdinLanguageCode = toCrowinLanguageCode(minecraftLanguageCode);

    if (crowdinLanguageCode.isEmpty()) {
      getLogger().warn("No Crowdin language code found for Minecraft language code: " + minecraftLanguageCode);
      return null;
    }

    getLogger().debug("Loading Crowdin translations for Minecraft language code: " + minecraftLanguageCode);

    try {
      if (checkDownloadNeeds(crowdinLanguageCode)) {
        tryDownload(crowdinLanguageCode);
      }
    } catch (IOException e) {
      getLogger().error("Error downloading translations", e);
    }

    try {
      Files.writeString(getTimestampFilePath(), (new Gson()).toJson(getTimestamp()));
      return Files.newInputStream(getTranslationsPath(minecraftLanguageCode));
    } catch (IOException e) {
      getLogger().error("Error open translations file", e);
    }

    return null;
  }

  private static class Timestamp {
    @Expose
    @SerializedName("last-crowdin-connection")
    private Map<String, Long> languageCodeByLastCrowdinConnection;
    @Expose
    @SerializedName("last-modified")
    private Map<String, Long> languageCodeByLastModified;

    public Timestamp() {
      languageCodeByLastCrowdinConnection = Maps.newHashMap();
      languageCodeByLastModified = Maps.newHashMap();
    }

    public long getLastConnection(String crowdinLanguageCode) {
      return languageCodeByLastCrowdinConnection.getOrDefault(crowdinLanguageCode, 0L);
    }

    public void setLastConnection(String crowdinLanguageCode, long lastConnection) {
      languageCodeByLastCrowdinConnection.put(crowdinLanguageCode, lastConnection);
    }

    public long getLastModified(String crowdinLanguageCode) {
      return languageCodeByLastModified.getOrDefault(crowdinLanguageCode, 0L);
    }

    public void setLastModified(String crowdinLanguageCode, long lastModified) {
      languageCodeByLastModified.put(crowdinLanguageCode, lastModified);
    }
  }
}
