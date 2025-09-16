package net.minecraft.client;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Queues;
import com.google.common.collect.UnmodifiableIterator;
import com.google.gson.JsonElement;
import com.mojang.authlib.AuthenticationService;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.OfflineSocialInteractions;
import com.mojang.authlib.minecraft.SocialInteractionsService;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.DisplayData;
import com.mojang.blaze3d.platform.GlUtil;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.WindowEventHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Function4;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.SocketAddress;
import java.nio.ByteOrder;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DatapackLoadFailureScreen;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.InBedChatScreen;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.OutOfMemoryScreen;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.WinScreen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.gui.screens.social.PlayerSocialManager;
import net.minecraft.client.gui.screens.social.SocialInteractionsScreen;
import net.minecraft.client.gui.screens.worldselection.EditWorldScreen;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.GpuWarnlistManager;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.VirtualScreen;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.ClientPackSource;
import net.minecraft.client.resources.FoliageColorReloadListener;
import net.minecraft.client.resources.GrassColorReloadListener;
import net.minecraft.client.resources.LegacyPackResourcesAdapter;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.client.resources.PackResourcesAdapterV4;
import net.minecraft.client.resources.PaintingTextureManager;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.client.resources.SplashManager;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.searchtree.MutableSearchTree;
import net.minecraft.client.searchtree.ReloadableIdSearchTree;
import net.minecraft.client.searchtree.ReloadableSearchTree;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.tutorial.Tutorial;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.KeybindComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.resources.RegistryReadOps;
import net.minecraft.resources.RegistryWriteOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerResources;
import net.minecraft.server.level.progress.ProcessorChunkProgressListener;
import net.minecraft.server.level.progress.StoringChunkProgressListener;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleReloadableResourceManager;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.FrameTimer;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.profiling.ContinuousProfiler;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.ResultField;
import net.minecraft.util.profiling.SingleTickProfiler;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Snooper;
import net.minecraft.world.SnooperPopulator;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PlayerHeadItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.WorldData;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Minecraft extends ReentrantBlockableEventLoop<Runnable> implements SnooperPopulator, WindowEventHandler {
   private static Minecraft instance;
   private static final Logger LOGGER = LogManager.getLogger();
   public static final boolean ON_OSX = Util.getPlatform() == Util.OS.OSX;
   public static final ResourceLocation DEFAULT_FONT = new ResourceLocation("default");
   public static final ResourceLocation UNIFORM_FONT = new ResourceLocation("uniform");
   public static final ResourceLocation ALT_FONT = new ResourceLocation("alt");
   private static final CompletableFuture<Unit> RESOURCE_RELOAD_INITIAL_TASK = CompletableFuture.completedFuture(Unit.INSTANCE);
   private static final Component SOCIAL_INTERACTIONS_NOT_AVAILABLE = new TranslatableComponent("multiplayer.socialInteractions.not_available");
   private final File resourcePackDirectory;
   private final PropertyMap profileProperties;
   private final TextureManager textureManager;
   private final DataFixer fixerUpper;
   private final VirtualScreen virtualScreen;
   private final Window window;
   private final Timer timer = new Timer(20.0F, 0L);
   private final Snooper snooper = new Snooper("client", this, Util.getMillis());
   private final RenderBuffers renderBuffers;
   public final LevelRenderer levelRenderer;
   private final EntityRenderDispatcher entityRenderDispatcher;
   private final ItemRenderer itemRenderer;
   private final ItemInHandRenderer itemInHandRenderer;
   public final ParticleEngine particleEngine;
   private final SearchRegistry searchRegistry = new SearchRegistry();
   private final User user;
   public final Font font;
   public final GameRenderer gameRenderer;
   public final DebugRenderer debugRenderer;
   private final AtomicReference<StoringChunkProgressListener> progressListener = new AtomicReference<>();
   public final Gui gui;
   public final Options options;
   private final HotbarManager hotbarManager;
   public final MouseHandler mouseHandler;
   public final KeyboardHandler keyboardHandler;
   public final File gameDirectory;
   private final String launchedVersion;
   private final String versionType;
   private final Proxy proxy;
   private final LevelStorageSource levelSource;
   public final FrameTimer frameTimer = new FrameTimer();
   private final boolean is64bit;
   private final boolean demo;
   private final boolean allowsMultiplayer;
   private final boolean allowsChat;
   private final ReloadableResourceManager resourceManager;
   private final ClientPackSource clientPackSource;
   private final PackRepository resourcePackRepository;
   private final LanguageManager languageManager;
   private final BlockColors blockColors;
   private final ItemColors itemColors;
   private final RenderTarget mainRenderTarget;
   private final SoundManager soundManager;
   private final MusicManager musicManager;
   private final FontManager fontManager;
   private final SplashManager splashManager;
   private final GpuWarnlistManager gpuWarnlistManager;
   private final MinecraftSessionService minecraftSessionService;
   private final SocialInteractionsService socialInteractionsService;
   private final SkinManager skinManager;
   private final ModelManager modelManager;
   private final BlockRenderDispatcher blockRenderer;
   private final PaintingTextureManager paintingTextures;
   private final MobEffectTextureManager mobEffectTextures;
   private final ToastComponent toast;
   private final Game game = new Game(this);
   private final Tutorial tutorial;
   private final PlayerSocialManager playerSocialManager;
   public static byte[] reserve = new byte[10485760];
   @Nullable
   public MultiPlayerGameMode gameMode;
   @Nullable
   public ClientLevel level;
   @Nullable
   public LocalPlayer player;
   @Nullable
   private IntegratedServer singleplayerServer;
   @Nullable
   private ServerData currentServer;
   @Nullable
   private Connection pendingConnection;
   private boolean isLocalServer;
   @Nullable
   public Entity cameraEntity;
   @Nullable
   public Entity crosshairPickEntity;
   @Nullable
   public HitResult hitResult;
   private int rightClickDelay;
   protected int missTime;
   private boolean pause;
   private float pausePartialTick;
   private long lastNanoTime = Util.getNanos();
   private long lastTime;
   private int frames;
   public boolean noRender;
   @Nullable
   public Screen screen;
   @Nullable
   public Overlay overlay;
   private boolean connectedToRealms;
   private Thread gameThread;
   private volatile boolean running = true;
   @Nullable
   private CrashReport delayedCrash;
   private static int fps;
   public String fpsString = "";
   public boolean chunkPath;
   public boolean chunkVisibility;
   public boolean smartCull = true;
   private boolean windowActive;
   private final Queue<Runnable> progressTasks = Queues.newConcurrentLinkedQueue();
   @Nullable
   private CompletableFuture<Void> pendingReload;
   @Nullable
   private TutorialToast socialInteractionsToast;
   private ProfilerFiller profiler = InactiveProfiler.INSTANCE;
   private int fpsPieRenderTicks;
   private final ContinuousProfiler fpsPieProfiler = new ContinuousProfiler(Util.timeSource, () -> this.fpsPieRenderTicks);
   @Nullable
   private ProfileResults fpsPieResults;
   private String debugPath = "root";

   public Minecraft(GameConfig var1) {
      super("Client");
      instance = this;
      this.gameDirectory = var1.location.gameDirectory;
      File var2xx = var1.location.assetDirectory;
      this.resourcePackDirectory = var1.location.resourcePackDirectory;
      this.launchedVersion = var1.game.launchVersion;
      this.versionType = var1.game.versionType;
      this.profileProperties = var1.user.profileProperties;
      this.clientPackSource = new ClientPackSource(new File(this.gameDirectory, "server-resource-packs"), var1.location.getAssetIndex());
      this.resourcePackRepository = new PackRepository(
              Minecraft::createClientPackAdapter, this.clientPackSource, new FolderRepositorySource(this.resourcePackDirectory, PackSource.DEFAULT)
      );
      this.proxy = var1.user.proxy;
      YggdrasilAuthenticationService var3xxx = new YggdrasilAuthenticationService(this.proxy);
      this.minecraftSessionService = var3xxx.createMinecraftSessionService();
      this.socialInteractionsService = this.createSocialInteractions(var3xxx, var1);
      this.user = var1.user.user;
      LOGGER.info("Setting user: {}", this.user.getName());
      LOGGER.debug("(Session ID is {})", this.user.getSessionId());
      this.demo = var1.game.demo;
      this.allowsMultiplayer = !var1.game.disableMultiplayer;
      this.allowsChat = !var1.game.disableChat;
      this.is64bit = checkIs64Bit();
      this.singleplayerServer = null;
      String var4;
      int var5x;
      if (this.allowsMultiplayer() && var1.server.hostname != null) {
         var4 = var1.server.hostname;
         var5x = var1.server.port;
      } else {
         var4 = null;
         var5x = 0;
      }

      KeybindComponent.setKeyResolver(KeyMapping::createNameSupplier);
      this.fixerUpper = DataFixers.getDataFixer();
      this.toast = new ToastComponent(this);
      this.tutorial = new Tutorial(this);
      this.gameThread = Thread.currentThread();
      this.options = new Options(this, this.gameDirectory);
      this.hotbarManager = new HotbarManager(this.gameDirectory, this.fixerUpper);
      LOGGER.info("Backend library: {}", RenderSystem.getBackendDescription());
      DisplayData var6;
      if (this.options.overrideHeight > 0 && this.options.overrideWidth > 0) {
         var6 = new DisplayData(
                 this.options.overrideWidth, this.options.overrideHeight, var1.display.fullscreenWidth, var1.display.fullscreenHeight, var1.display.isFullscreen
         );
      } else {
         var6 = var1.display;
      }

      Util.timeSource = RenderSystem.initBackendSystem();
      this.virtualScreen = new VirtualScreen(this);
      this.window = this.virtualScreen.newWindow(var6, this.options.fullscreenVideoModeString, this.createTitle());
      this.setWindowActive(true);

      try {
         InputStream var7 = this.getClientPackSource().getVanillaPack().getResource(PackType.CLIENT_RESOURCES, new ResourceLocation("icons/icon_16x16.png"));
         InputStream var8x = this.getClientPackSource().getVanillaPack().getResource(PackType.CLIENT_RESOURCES, new ResourceLocation("icons/icon_32x32.png"));
         this.window.setIcon(var7, var8x);
      } catch (IOException var9) {
         LOGGER.error("Couldn't set icon", var9);
      }

      this.window.setFramerateLimit(this.options.framerateLimit);
      this.mouseHandler = new MouseHandler(this);
      this.mouseHandler.setup(this.window.getWindow());
      this.keyboardHandler = new KeyboardHandler(this);
      this.keyboardHandler.setup(this.window.getWindow());
      RenderSystem.initRenderer(this.options.glDebugVerbosity, false);
      this.mainRenderTarget = new RenderTarget(this.window.getWidth(), this.window.getHeight(), true, ON_OSX);
      this.mainRenderTarget.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
      this.resourceManager = new SimpleReloadableResourceManager(PackType.CLIENT_RESOURCES);
      this.resourcePackRepository.reload();
      this.options.loadSelectedResourcePacks(this.resourcePackRepository);
      this.languageManager = new LanguageManager(this.options.languageCode);
      this.resourceManager.registerReloadListener(this.languageManager);
      this.textureManager = new TextureManager(this.resourceManager);
      this.resourceManager.registerReloadListener(this.textureManager);
      this.skinManager = new SkinManager(this.textureManager, new File(var2xx, "skins"), this.minecraftSessionService);
      this.levelSource = new LevelStorageSource(this.gameDirectory.toPath().resolve("saves"), this.gameDirectory.toPath().resolve("backups"), this.fixerUpper);
      this.soundManager = new SoundManager(this.resourceManager, this.options);
      this.resourceManager.registerReloadListener(this.soundManager);
      this.splashManager = new SplashManager(this.user);
      this.resourceManager.registerReloadListener(this.splashManager);
      this.musicManager = new MusicManager(this);
      this.fontManager = new FontManager(this.textureManager);
      this.font = this.fontManager.createFont();
      this.resourceManager.registerReloadListener(this.fontManager.getReloadListener());
      this.selectMainFont(this.isEnforceUnicode());
      this.resourceManager.registerReloadListener(new GrassColorReloadListener());
      this.resourceManager.registerReloadListener(new FoliageColorReloadListener());
      this.window.setErrorSection("Startup");
      RenderSystem.setupDefaultState(0, 0, this.window.getWidth(), this.window.getHeight());
      this.window.setErrorSection("Post startup");
      this.blockColors = BlockColors.createDefault();
      this.itemColors = ItemColors.createDefault(this.blockColors);
      this.modelManager = new ModelManager(this.textureManager, this.blockColors, this.options.mipmapLevels);
      this.resourceManager.registerReloadListener(this.modelManager);
      this.itemRenderer = new ItemRenderer(this.textureManager, this.modelManager, this.itemColors);
      this.entityRenderDispatcher = new EntityRenderDispatcher(this.textureManager, this.itemRenderer, this.resourceManager, this.font, this.options);
      this.itemInHandRenderer = new ItemInHandRenderer(this);
      this.resourceManager.registerReloadListener(this.itemRenderer);
      this.renderBuffers = new RenderBuffers();
      this.gameRenderer = new GameRenderer(this, this.resourceManager, this.renderBuffers);
      this.resourceManager.registerReloadListener(this.gameRenderer);
      this.playerSocialManager = new PlayerSocialManager(this, this.socialInteractionsService);
      this.blockRenderer = new BlockRenderDispatcher(this.modelManager.getBlockModelShaper(), this.blockColors);
      this.resourceManager.registerReloadListener(this.blockRenderer);
      this.levelRenderer = new LevelRenderer(this, this.renderBuffers);
      this.resourceManager.registerReloadListener(this.levelRenderer);
      this.createSearchTrees();
      this.resourceManager.registerReloadListener(this.searchRegistry);
      this.particleEngine = new ParticleEngine(this.level, this.textureManager);
      this.resourceManager.registerReloadListener(this.particleEngine);
      this.paintingTextures = new PaintingTextureManager(this.textureManager);
      this.resourceManager.registerReloadListener(this.paintingTextures);
      this.mobEffectTextures = new MobEffectTextureManager(this.textureManager);
      this.resourceManager.registerReloadListener(this.mobEffectTextures);
      this.gpuWarnlistManager = new GpuWarnlistManager();
      this.resourceManager.registerReloadListener(this.gpuWarnlistManager);
      this.gui = new Gui(this);
      this.debugRenderer = new DebugRenderer(this);
      RenderSystem.setErrorCallback(this::onFullscreenError);
      if (this.options.fullscreen && !this.window.isFullscreen()) {
         this.window.toggleFullScreen();
         this.options.fullscreen = this.window.isFullscreen();
      }

      this.window.updateVsync(this.options.enableVsync);
      this.window.updateRawMouseInput(this.options.rawMouseInput);
      this.window.setDefaultErrorCallback();
      this.resizeDisplay();
      if (var4 != null) {
         this.setScreen(new ConnectScreen(new TitleScreen(), this, var4, var5x));
      } else {
         this.setScreen(new TitleScreen(true));
      }

      LoadingOverlay.registerTextures(this);
      List<PackResources> var10 = this.resourcePackRepository.openAllSelected();
      this.setOverlay(
              new LoadingOverlay(
                      this,
                      this.resourceManager.createFullReload(Util.backgroundExecutor(), this, RESOURCE_RELOAD_INITIAL_TASK, var10),
                      var1x -> Util.ifElse(var1x, this::rollbackResourcePacks, () -> {
                         if (SharedConstants.IS_RUNNING_IN_IDE) {
                            this.selfTest();
                         }
                      }),
                      false
              )
      );
   }

   public void updateTitle() {
      this.window.setTitle(this.createTitle());
   }

   private String createTitle() {
      StringBuilder var1 = new StringBuilder("Minecraft");
      if (this.isProbablyModded()) {
         var1.append("*");
      }

      var1.append(" ");
      var1.append(SharedConstants.getCurrentVersion().getName());
      ClientPacketListener var2 = this.getConnection();
      if (var2 != null && var2.getConnection().isConnected()) {
         var1.append(" - ");
         if (this.singleplayerServer != null && !this.singleplayerServer.isPublished()) {
            var1.append(I18n.get("title.singleplayer"));
         } else if (this.isConnectedToRealms()) {
            var1.append(I18n.get("title.multiplayer.realms"));
         } else if (this.singleplayerServer == null && (this.currentServer == null || !this.currentServer.isLan())) {
            var1.append(I18n.get("title.multiplayer.other"));
         } else {
            var1.append(I18n.get("title.multiplayer.lan"));
         }
      }

      return var1.toString();
   }

   private SocialInteractionsService createSocialInteractions(YggdrasilAuthenticationService var1, GameConfig var2) {
      try {
         return var1.createSocialInteractionsService(var2.user.user.getAccessToken());
      } catch (AuthenticationException var4) {
         LOGGER.error("Failed to verify authentication", var4);
         return new OfflineSocialInteractions();
      }
   }

   public boolean isProbablyModded() {
      return !"vanilla".equals(ClientBrandRetriever.getClientModName()) || Minecraft.class.getSigners() == null;
   }

   private void rollbackResourcePacks(Throwable var1) {
      if (this.resourcePackRepository.getSelectedIds().size() > 1) {
         Component var2;
         if (var1 instanceof SimpleReloadableResourceManager.ResourcePackLoadingFailure) {
            var2 = new TextComponent(((SimpleReloadableResourceManager.ResourcePackLoadingFailure)var1).getPack().getName());
         } else {
            var2 = null;
         }

         this.clearResourcePacksOnError(var1, var2);
      } else {
         Util.throwAsRuntime(var1);
      }
   }

   public void clearResourcePacksOnError(Throwable var1, @Nullable Component var2) {
      LOGGER.info("Caught error loading resourcepacks, removing all selected resourcepacks", var1);
      this.resourcePackRepository.setSelected(Collections.emptyList());
      this.options.resourcePacks.clear();
      this.options.incompatibleResourcePacks.clear();
      this.options.save();
      this.reloadResourcePacks().thenRun(() -> {
         ToastComponent var2x = this.getToasts();
         SystemToast.addOrUpdate(var2x, SystemToast.SystemToastIds.PACK_LOAD_FAILURE, new TranslatableComponent("resourcePack.load_fail"), var2);
      });
   }

   public void run() {
      this.gameThread = Thread.currentThread();

      try {
         boolean var1 = false;

         while(this.running) {
            if (this.delayedCrash != null) {
               crash(this.delayedCrash);
               return;
            }

            try {
               SingleTickProfiler var7x = SingleTickProfiler.createTickProfiler("Renderer");
               boolean var3xx = this.shouldRenderFpsPie();
               this.startProfilers(var3xx, var7x);
               this.profiler.startTick();
               this.runTick(!var1);
               this.profiler.endTick();
               this.finishProfilers(var3xx, var7x);
            } catch (OutOfMemoryError var4) {
               if (var1) {
                  throw var4;
               }

               this.emergencySave();
               this.setScreen(new OutOfMemoryScreen());
               System.gc();
               LOGGER.fatal("Out of memory", var4);
               var1 = true;
            }
         }
      } catch (ReportedException var5) {
         this.fillReport(var5.getReport());
         this.emergencySave();
         LOGGER.fatal("Reported exception thrown!", var5);
         crash(var5.getReport());
      } catch (Throwable var6) {
         CrashReport var2 = this.fillReport(new CrashReport("Unexpected error", var6));
         LOGGER.fatal("Unreported exception thrown!", var6);
         this.emergencySave();
         crash(var2);
      }
   }

   void selectMainFont(boolean var1) {
      this.fontManager.setRenames(var1 ? ImmutableMap.of(DEFAULT_FONT, UNIFORM_FONT) : ImmutableMap.of());
   }

   private void createSearchTrees() {
      ReloadableSearchTree<ItemStack> var1 = new ReloadableSearchTree<>(
              var0 -> var0.getTooltipLines(null, TooltipFlag.Default.NORMAL)
                      .stream()
                      .map(var0x -> ChatFormatting.stripFormatting(var0x.getString()).trim())
                      .filter(var0x -> !var0x.isEmpty()),
              var0 -> Stream.of(Registry.ITEM.getKey(var0.getItem()))
      );
      ReloadableIdSearchTree<ItemStack> var2_x = new ReloadableIdSearchTree<>(var0 -> ItemTags.getAllTags().getMatchingTags(var0.getItem()).stream());
      NonNullList<ItemStack> var3xx = NonNullList.create();

      for(Item var5 : Registry.ITEM) {
         var5.fillItemCategory(CreativeModeTab.TAB_SEARCH, var3xx);
      }

      var3xx.forEach(var2x -> {
         var1.add(var2x);
         var2_x.add(var2x);
      });
      ReloadableSearchTree<RecipeCollection> var6xxx = new ReloadableSearchTree<>(
              var0 -> var0.getRecipes()
                      .stream()
                      .flatMap(var0x -> var0x.getResultItem().getTooltipLines(null, TooltipFlag.Default.NORMAL).stream())
                      .map(var0x -> ChatFormatting.stripFormatting(var0x.getString()).trim())
                      .filter(var0x -> !var0x.isEmpty()),
              var0 -> var0.getRecipes().stream().map(var0x -> Registry.ITEM.getKey(var0x.getResultItem().getItem()))
      );
      this.searchRegistry.register(SearchRegistry.CREATIVE_NAMES, var1);
      this.searchRegistry.register(SearchRegistry.CREATIVE_TAGS, var2_x);
      this.searchRegistry.register(SearchRegistry.RECIPE_COLLECTIONS, var6xxx);
   }

   private void onFullscreenError(int var1, long var2) {
      this.options.enableVsync = false;
      this.options.save();
   }

   private static boolean checkIs64Bit() {
      String[] var0 = new String[]{"sun.arch.data.model", "com.ibm.vm.bitmode", "os.arch"};

      for(String var4 : var0) {
         String var5x = System.getProperty(var4);
         if (var5x != null && var5x.contains("64")) {
            return true;
         }
      }

      return false;
   }

   public RenderTarget getMainRenderTarget() {
      return this.mainRenderTarget;
   }

   public String getLaunchedVersion() {
      return this.launchedVersion;
   }

   public String getVersionType() {
      return this.versionType;
   }

   public void delayCrash(CrashReport var1) {
      this.delayedCrash = var1;
   }

   public static void crash(CrashReport var0) {
      File var1 = new File(getInstance().gameDirectory, "crash-reports");
      File var2x = new File(var1, "crash-" + new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date()) + "-client.txt");
      Bootstrap.realStdoutPrintln(var0.getFriendlyReport());
      if (var0.getSaveFile() != null) {
         Bootstrap.realStdoutPrintln("#@!@# Game crashed! Crash report saved to: #@!@# " + var0.getSaveFile());
         System.exit(-1);
      } else if (var0.saveToFile(var2x)) {
         Bootstrap.realStdoutPrintln("#@!@# Game crashed! Crash report saved to: #@!@# " + var2x.getAbsolutePath());
         System.exit(-1);
      } else {
         Bootstrap.realStdoutPrintln("#@?@# Game crashed! Crash report could not be saved. #@?@#");
         System.exit(-2);
      }
   }

   public boolean isEnforceUnicode() {
      return this.options.forceUnicodeFont;
   }

   public CompletableFuture<Void> reloadResourcePacks() {
      if (this.pendingReload != null) {
         return this.pendingReload;
      } else {
         CompletableFuture<Void> var1 = new CompletableFuture<>();
         if (this.overlay instanceof LoadingOverlay) {
            this.pendingReload = var1;
            return var1;
         } else {
            this.resourcePackRepository.reload();
            List<PackResources> var2 = this.resourcePackRepository.openAllSelected();
            this.setOverlay(
                    new LoadingOverlay(
                            this,
                            this.resourceManager.createFullReload(Util.backgroundExecutor(), this, RESOURCE_RELOAD_INITIAL_TASK, var2),
                            var2x -> Util.ifElse(var2x, this::rollbackResourcePacks, () -> {
                               this.levelRenderer.allChanged();
                               var1.complete(null);
                            }),
                            true
                    )
            );
            return var1;
         }
      }
   }

   private void selfTest() {
      boolean var1 = false;
      BlockModelShaper var2x = this.getBlockRenderer().getBlockModelShaper();
      BakedModel var3xx = var2x.getModelManager().getMissingModel();

      for(Block var5 : Registry.BLOCK) {
         UnmodifiableIterator var6 = var5.getStateDefinition().getPossibleStates().iterator();

         while(var6.hasNext()) {
            BlockState var7xxx = (BlockState)var6.next();
            if (var7xxx.getRenderShape() == RenderShape.MODEL) {
               BakedModel var8xxxx = var2x.getBlockModel(var7xxx);
               if (var8xxxx == var3xx) {
                  LOGGER.debug("Missing model for: {}", var7xxx);
                  var1 = true;
               }
            }
         }
      }

      TextureAtlasSprite var13xxx = var3xx.getParticleIcon();

      for(Block var16 : Registry.BLOCK) {
         UnmodifiableIterator var18 = var16.getStateDefinition().getPossibleStates().iterator();

         while(var18.hasNext()) {
            BlockState var20xxxx = (BlockState)var18.next();
            TextureAtlasSprite var9xxxxx = var2x.getParticleIcon(var20xxxx);
            if (!var20xxxx.isAir() && var9xxxxx == var13xxx) {
               LOGGER.debug("Missing particle icon for: {}", var20xxxx);
               var1 = true;
            }
         }
      }

      NonNullList<ItemStack> var15xxxx = NonNullList.create();

      for(Item var19 : Registry.ITEM) {
         var15xxxx.clear();
         var19.fillItemCategory(CreativeModeTab.TAB_SEARCH, var15xxxx);

         for(ItemStack var22 : var15xxxx) {
            String var10xxxxx = var22.getDescriptionId();
            String var11xxxxxx = new TranslatableComponent(var10xxxxx).getString();
            if (var11xxxxxx.toLowerCase(Locale.ROOT).equals(var19.getDescriptionId())) {
               LOGGER.debug("Missing translation for: {} {} {}", var22, var10xxxxx, var22.getItem());
            }
         }
      }

      var1 |= MenuScreens.selfTest();
      if (var1) {
         throw new IllegalStateException("Your game data is foobar, fix the errors above!");
      }
   }

   public LevelStorageSource getLevelSource() {
      return this.levelSource;
   }

   private void openChatScreen(String var1) {
      if (this.isLocalServer() || this.allowsChat()) {
         this.setScreen(new ChatScreen(var1));
      } else if (this.player != null) {
         this.player.sendMessage(new TranslatableComponent("chat.cannotSend").withStyle(ChatFormatting.RED), Util.NIL_UUID);
      }
   }

   public void setScreen(@Nullable Screen var1) {
      if (this.screen != null) {
         this.screen.removed();
      }

      if (var1 == null && this.level == null) {
         var1 = new TitleScreen();
      } else if (var1 == null && this.player.isDeadOrDying()) {
         if (this.player.shouldShowDeathScreen()) {
            var1 = new DeathScreen(null, this.level.getLevelData().isHardcore());
         } else {
            this.player.respawn();
         }
      }

      if (var1 instanceof TitleScreen || var1 instanceof JoinMultiplayerScreen) {
         this.options.renderDebug = false;
         this.gui.getChat().clearMessages(true);
      }

      this.screen = var1;
      if (var1 != null) {
         this.mouseHandler.releaseMouse();
         KeyMapping.releaseAll();
         var1.init(this, this.window.getGuiScaledWidth(), this.window.getGuiScaledHeight());
         this.noRender = false;
         NarratorChatListener.INSTANCE.sayNow(var1.getNarrationMessage());
      } else {
         this.soundManager.resume();
         this.mouseHandler.grabMouse();
      }

      this.updateTitle();
   }

   public void setOverlay(@Nullable Overlay var1) {
      this.overlay = var1;
   }

   public void destroy() {
      try {
         LOGGER.info("Stopping!");

         try {
            NarratorChatListener.INSTANCE.destroy();
         } catch (Throwable var7) {
         }

         try {
            if (this.level != null) {
               this.level.disconnect();
            }

            this.clearLevel();
         } catch (Throwable var6) {
         }

         if (this.screen != null) {
            this.screen.removed();
         }

         this.close();
      } finally {
         Util.timeSource = System::nanoTime;
         if (this.delayedCrash == null) {
            System.exit(0);
         }
      }
   }

   @Override
   public void close() {
      try {
         this.modelManager.close();
         this.fontManager.close();
         this.gameRenderer.close();
         this.levelRenderer.close();
         this.soundManager.destroy();
         this.resourcePackRepository.close();
         this.particleEngine.close();
         this.mobEffectTextures.close();
         this.paintingTextures.close();
         this.textureManager.close();
         this.resourceManager.close();
         Util.shutdownExecutors();
      } catch (Throwable var5) {
         LOGGER.error("Shutdown failure!", var5);
         throw var5;
      } finally {
         this.virtualScreen.close();
         this.window.close();
      }
   }

   private void runTick(boolean var1) {
      this.window.setErrorSection("Pre render");
      long var2 = Util.getNanos();
      if (this.window.shouldClose()) {
         this.stop();
      }

      if (this.pendingReload != null && !(this.overlay instanceof LoadingOverlay)) {
         CompletableFuture<Void> var4 = this.pendingReload;
         this.pendingReload = null;
         this.reloadResourcePacks().thenRun(() -> var4.complete(null));
      }

      Runnable var9;
      while((var9 = this.progressTasks.poll()) != null) {
         var9.run();
      }

      if (var1) {
         int var5 = this.timer.advanceTime(Util.getMillis());
         this.profiler.push("scheduledExecutables");
         this.runAllTasks();
         this.profiler.pop();
         this.profiler.push("tick");

         for(int var6x = 0; var6x < Math.min(10, var5); ++var6x) {
            this.profiler.incrementCounter("clientTick");
            this.tick();
         }

         this.profiler.pop();
      }

      this.mouseHandler.turnPlayer();
      this.window.setErrorSection("Render");
      this.profiler.push("sound");
      this.soundManager.updateSource(this.gameRenderer.getMainCamera());
      this.profiler.pop();
      this.profiler.push("render");
      RenderSystem.pushMatrix();
      RenderSystem.clear(16640, ON_OSX);
      this.mainRenderTarget.bindWrite(true);
      FogRenderer.setupNoFog();
      this.profiler.push("display");
      RenderSystem.enableTexture();
      RenderSystem.enableCull();
      this.profiler.pop();
      if (!this.noRender) {
         this.profiler.popPush("gameRenderer");
         this.gameRenderer.render(this.pause ? this.pausePartialTick : this.timer.partialTick, var2, var1);
         this.profiler.popPush("toasts");
         this.toast.render(new PoseStack());
         this.profiler.pop();
      }

      if (this.fpsPieResults != null) {
         this.profiler.push("fpsPie");
         this.renderFpsMeter(new PoseStack(), this.fpsPieResults);
         this.profiler.pop();
      }

      this.profiler.push("blit");
      this.mainRenderTarget.unbindWrite();
      RenderSystem.popMatrix();
      RenderSystem.pushMatrix();
      this.mainRenderTarget.blitToScreen(this.window.getWidth(), this.window.getHeight());
      RenderSystem.popMatrix();
      this.profiler.popPush("updateDisplay");
      this.window.updateDisplay();
      int var10 = this.getFramerateLimit();
      if ((double)var10 < Option.FRAMERATE_LIMIT.getMaxValue()) {
         RenderSystem.limitDisplayFPS(var10);
      }

      this.profiler.popPush("yield");
      Thread.yield();
      this.profiler.pop();
      this.window.setErrorSection("Post render");
      ++this.frames;
      boolean var11 = this.hasSingleplayerServer()
              && (this.screen != null && this.screen.isPauseScreen() || this.overlay != null && this.overlay.isPauseScreen())
              && !this.singleplayerServer.isPublished();
      if (this.pause != var11) {
         if (this.pause) {
            this.pausePartialTick = this.timer.partialTick;
         } else {
            this.timer.partialTick = this.pausePartialTick;
         }

         this.pause = var11;
      }

      long var7 = Util.getNanos();
      this.frameTimer.logFrameDuration(var7 - this.lastNanoTime);
      this.lastNanoTime = var7;
      this.profiler.push("fpsUpdate");

      while(Util.getMillis() >= this.lastTime + 1000L) {
         fps = this.frames;
         this.fpsString = String.format(
                 "%d fps T: %s%s%s%s B: %d",
                 fps,
                 (double)this.options.framerateLimit == Option.FRAMERATE_LIMIT.getMaxValue() ? "inf" : this.options.framerateLimit,
                 this.options.enableVsync ? " vsync" : "",
                 this.options.graphicsMode.toString(),
                 this.options.renderClouds == CloudStatus.OFF ? "" : (this.options.renderClouds == CloudStatus.FAST ? " fast-clouds" : " fancy-clouds"),
                 this.options.biomeBlendRadius
         );
         this.lastTime += 1000L;
         this.frames = 0;
         this.snooper.prepare();
         if (!this.snooper.isStarted()) {
            this.snooper.start();
         }
      }

      this.profiler.pop();
   }

   private boolean shouldRenderFpsPie() {
      return this.options.renderDebug && this.options.renderDebugCharts && !this.options.hideGui;
   }

   private void startProfilers(boolean var1, @Nullable SingleTickProfiler var2) {
      if (var1) {
         if (!this.fpsPieProfiler.isEnabled()) {
            this.fpsPieRenderTicks = 0;
            this.fpsPieProfiler.enable();
         }

         ++this.fpsPieRenderTicks;
      } else {
         this.fpsPieProfiler.disable();
      }

      this.profiler = SingleTickProfiler.decorateFiller(this.fpsPieProfiler.getFiller(), var2);
   }

   private void finishProfilers(boolean var1, @Nullable SingleTickProfiler var2) {
      if (var2 != null) {
         var2.endTick();
      }

      if (var1) {
         this.fpsPieResults = this.fpsPieProfiler.getResults();
      } else {
         this.fpsPieResults = null;
      }

      this.profiler = this.fpsPieProfiler.getFiller();
   }

   @Override
   public void resizeDisplay() {
      int var1 = this.window.calculateScale(this.options.guiScale, this.isEnforceUnicode());
      this.window.setGuiScale((double)var1);
      if (this.screen != null) {
         this.screen.resize(this, this.window.getGuiScaledWidth(), this.window.getGuiScaledHeight());
      }

      RenderTarget var2 = this.getMainRenderTarget();
      var2.resize(this.window.getWidth(), this.window.getHeight(), ON_OSX);
      this.gameRenderer.resize(this.window.getWidth(), this.window.getHeight());
      this.mouseHandler.setIgnoreFirstMove();
   }

   @Override
   public void cursorEntered() {
      this.mouseHandler.cursorEntered();
   }

   private int getFramerateLimit() {
      return this.level != null || this.screen == null && this.overlay == null ? this.window.getFramerateLimit() : 60;
   }

   public void emergencySave() {
      try {
         reserve = new byte[0];
         this.levelRenderer.clear();
      } catch (Throwable var3) {
      }

      try {
         System.gc();
         if (this.isLocalServer && this.singleplayerServer != null) {
            this.singleplayerServer.halt(true);
         }

         this.clearLevel(new GenericDirtMessageScreen(new TranslatableComponent("menu.savingLevel")));
      } catch (Throwable var2) {
      }

      System.gc();
   }

   void debugFpsMeterKeyPress(int var1) {
      if (this.fpsPieResults != null) {
         List<ResultField> var2 = this.fpsPieResults.getTimes(this.debugPath);
         if (!var2.isEmpty()) {
            ResultField var3x = var2.remove(0);
            if (var1 == 0) {
               if (!var3x.name.isEmpty()) {
                  int var4xx = this.debugPath.lastIndexOf(30);
                  if (var4xx >= 0) {
                     this.debugPath = this.debugPath.substring(0, var4xx);
                  }
               }
            } else {
               --var1;
               if (var1 < var2.size() && !"unspecified".equals(var2.get(var1).name)) {
                  if (!this.debugPath.isEmpty()) {
                     this.debugPath = this.debugPath + '\u001e';
                  }

                  this.debugPath = this.debugPath + var2.get(var1).name;
               }
            }
         }
      }
   }

   private void renderFpsMeter(PoseStack var1, ProfileResults var2) {
      List<ResultField> var3 = var2.getTimes(this.debugPath);
      ResultField var4x = var3.remove(0);
      RenderSystem.clear(256, ON_OSX);
      RenderSystem.matrixMode(5889);
      RenderSystem.loadIdentity();
      RenderSystem.ortho(0.0, (double)this.window.getWidth(), (double)this.window.getHeight(), 0.0, 1000.0, 3000.0);
      RenderSystem.matrixMode(5888);
      RenderSystem.loadIdentity();
      RenderSystem.translatef(0.0F, 0.0F, -2000.0F);
      RenderSystem.lineWidth(1.0F);
      RenderSystem.disableTexture();
      Tesselator var5xx = Tesselator.getInstance();
      BufferBuilder var6xxx = var5xx.getBuilder();
      int var7xxxx = 160;
      int var8xxxxx = this.window.getWidth() - 160 - 10;
      int var9xxxxxx = this.window.getHeight() - 320;
      RenderSystem.enableBlend();
      var6xxx.begin(7, DefaultVertexFormat.POSITION_COLOR);
      var6xxx.vertex(((float)var8xxxxx - 176.0F), ((float)var9xxxxxx - 96.0F - 16.0F), 0.0).color(200, 0, 0, 0).endVertex();
      var6xxx.vertex(((float)var8xxxxx - 176.0F), (var9xxxxxx + 320), 0.0).color(200, 0, 0, 0).endVertex();
      var6xxx.vertex(((float)var8xxxxx + 176.0F), (var9xxxxxx + 320), 0.0).color(200, 0, 0, 0).endVertex();
      var6xxx.vertex(((float)var8xxxxx + 176.0F), ((float)var9xxxxxx - 96.0F - 16.0F), 0.0).color(200, 0, 0, 0).endVertex();
      var5xx.end();
      RenderSystem.disableBlend();
      double var10xxxxxxx = 0.0;

      for(ResultField var13 : var3) {
         int var14xxxxxxxx = Mth.floor(var13.percentage / 4.0) + 1;
         var6xxx.begin(6, DefaultVertexFormat.POSITION_COLOR);
         int var15xxxxxxxxx = var13.getColor();
         int var16xxxxxxxxxx = var15xxxxxxxxx >> 16 & 0xFF;
         int var17xxxxxxxxxxx = var15xxxxxxxxx >> 8 & 0xFF;
         int var18xxxxxxxxxxxx = var15xxxxxxxxx & 0xFF;
         var6xxx.vertex(var8xxxxx, var9xxxxxx, 0.0).color(var16xxxxxxxxxx, var17xxxxxxxxxxx, var18xxxxxxxxxxxx, 255).endVertex();

         for(int var19xxxxxxxxxxxxx = var14xxxxxxxx; var19xxxxxxxxxxxxx >= 0; --var19xxxxxxxxxxxxx) {
            float var20xxxxxxxxxxxxxx = (float)((var10xxxxxxx + var13.percentage * (double)var19xxxxxxxxxxxxx / (double)var14xxxxxxxx) * (float) (Math.PI * 2) / 100.0);
            float var21xxxxxxxxxxxxxxx = Mth.sin(var20xxxxxxxxxxxxxx) * 160.0F;
            float var22xxxxxxxxxxxxxxxx = Mth.cos(var20xxxxxxxxxxxxxx) * 160.0F * 0.5F;
            var6xxx.vertex(((float)var8xxxxx + var21xxxxxxxxxxxxxxx), ((float)var9xxxxxx - var22xxxxxxxxxxxxxxxx), 0.0)
                    .color(var16xxxxxxxxxx, var17xxxxxxxxxxx, var18xxxxxxxxxxxx, 255)
                    .endVertex();
         }

         var5xx.end();
         var6xxx.begin(5, DefaultVertexFormat.POSITION_COLOR);

         for(int var35xxxxxxxxxxxxx = var14xxxxxxxx; var35xxxxxxxxxxxxx >= 0; --var35xxxxxxxxxxxxx) {
            float var36xxxxxxxxxxxxxx = (float)((var10xxxxxxx + var13.percentage * (double)var35xxxxxxxxxxxxx / (double)var14xxxxxxxx) * (float) (Math.PI * 2) / 100.0);
            float var37xxxxxxxxxxxxxxx = Mth.sin(var36xxxxxxxxxxxxxx) * 160.0F;
            float var38xxxxxxxxxxxxxxxx = Mth.cos(var36xxxxxxxxxxxxxx) * 160.0F * 0.5F;
            if (!(var38xxxxxxxxxxxxxxxx > 0.0F)) {
               var6xxx.vertex(((float)var8xxxxx + var37xxxxxxxxxxxxxxx), ((float)var9xxxxxx - var38xxxxxxxxxxxxxxxx), 0.0)
                       .color(var16xxxxxxxxxx >> 1, var17xxxxxxxxxxx >> 1, var18xxxxxxxxxxxx >> 1, 255)
                       .endVertex();
               var6xxx.vertex(((float)var8xxxxx + var37xxxxxxxxxxxxxxx), ((float)var9xxxxxx - var38xxxxxxxxxxxxxxxx + 10.0F), 0.0)
                       .color(var16xxxxxxxxxx >> 1, var17xxxxxxxxxxx >> 1, var18xxxxxxxxxxxx >> 1, 255)
                       .endVertex();
            }
         }

         var5xx.end();
         var10xxxxxxx += var13.percentage;
      }

      DecimalFormat var23xxxxxxxx = new DecimalFormat("##0.00");
      var23xxxxxxxx.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
      RenderSystem.enableTexture();
      String var24xxxxxxxxx = ProfileResults.demanglePath(var4x.name);
      String var26xxxxxxxxxx = "";
      if (!"unspecified".equals(var24xxxxxxxxx)) {
         var26xxxxxxxxxx = var26xxxxxxxxxx + "[0] ";
      }

      if (var24xxxxxxxxx.isEmpty()) {
         var26xxxxxxxxxx = var26xxxxxxxxxx + "ROOT ";
      } else {
         var26xxxxxxxxxx = var26xxxxxxxxxx + var24xxxxxxxxx + ' ';
      }

      int var30xxxxxxxx = 16777215;
      this.font.drawShadow(var1, var26xxxxxxxxxx, (float)(var8xxxxx - 160), (float)(var9xxxxxx - 80 - 16), 16777215);
      var26xxxxxxxxxx = var23xxxxxxxx.format(var4x.globalPercentage) + "%";
      this.font.drawShadow(var1, var26xxxxxxxxxx, (float)(var8xxxxx + 160 - this.font.width(var26xxxxxxxxxx)), (float)(var9xxxxxx - 80 - 16), 16777215);

      for(int var25xxxxxxxxx = 0; var25xxxxxxxxx < var3.size(); ++var25xxxxxxxxx) {
         ResultField var29xxxxxxxxxx = var3.get(var25xxxxxxxxx);
         StringBuilder var31xxxxxxxxxxx = new StringBuilder();
         if ("unspecified".equals(var29xxxxxxxxxx.name)) {
            var31xxxxxxxxxxx.append("[?] ");
         } else {
            var31xxxxxxxxxxx.append("[").append(var25xxxxxxxxx + 1).append("] ");
         }

         String var32xxxxxxxxxx = var31xxxxxxxxxxx.append(var29xxxxxxxxxx.name).toString();
         this.font.drawShadow(
                 var1, var32xxxxxxxxxx, (float)(var8xxxxx - 160), (float)(var9xxxxxx + 80 + var25xxxxxxxxx * 8 + 20), var29xxxxxxxxxx.getColor()
         );
         var32xxxxxxxxxx = var23xxxxxxxx.format(var29xxxxxxxxxx.percentage) + "%";
         this.font
                 .drawShadow(
                         var1, var32xxxxxxxxxx, (float)(var8xxxxx + 160 - 50 - this.font.width(var32xxxxxxxxxx)), (float)(var9xxxxxx + 80 + var25xxxxxxxxx * 8 + 20), var29xxxxxxxxxx.getColor()
                 );
         var32xxxxxxxxxx = var23xxxxxxxx.format(var29xxxxxxxxxx.globalPercentage) + "%";
         this.font
                 .drawShadow(
                         var1, var32xxxxxxxxxx, (float)(var8xxxxx + 160 - this.font.width(var32xxxxxxxxxx)), (float)(var9xxxxxx + 80 + var25xxxxxxxxx * 8 + 20), var29xxxxxxxxxx.getColor()
                 );
      }
   }

   public void stop() {
      this.running = false;
   }

   public boolean isRunning() {
      return this.running;
   }

   public void pauseGame(boolean var1) {
      if (this.screen == null) {
         boolean var2 = this.hasSingleplayerServer() && !this.singleplayerServer.isPublished();
         if (var2) {
            this.setScreen(new PauseScreen(!var1));
            this.soundManager.pause();
         } else {
            this.setScreen(new PauseScreen(true));
         }
      }
   }

   private void continueAttack(boolean var1) {
      if (!var1) {
         this.missTime = 0;
      }

      if (this.missTime <= 0 && !this.player.isUsingItem()) {
         if (var1 && this.hitResult != null && this.hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult var2 = (BlockHitResult)this.hitResult;
            BlockPos var3x = var2.getBlockPos();
            if (!this.level.getBlockState(var3x).isAir()) {
               Direction var3xx = var2.getDirection();
               if (this.gameMode.continueDestroyBlock(var3x, var3xx)) {
                  this.particleEngine.crack(var3x, var3xx);
                  this.player.swing(InteractionHand.MAIN_HAND);
               }
            }
         } else {
            this.gameMode.stopDestroyBlock();
         }
      }
   }

   private void startAttack() {
      if (this.missTime <= 0) {
         if (this.hitResult == null) {
            LOGGER.error("Null returned as 'hitResult', this shouldn't happen!");
            if (this.gameMode.hasMissTime()) {
               this.missTime = 10;
            }
         } else if (!this.player.isHandsBusy()) {
            switch(this.hitResult.getType()) {
               case ENTITY:
                  this.gameMode.attack(this.player, ((EntityHitResult)this.hitResult).getEntity());
                  break;
               case BLOCK:
                  BlockHitResult var1 = (BlockHitResult)this.hitResult;
                  BlockPos var2x = var1.getBlockPos();
                  if (!this.level.getBlockState(var2x).isAir()) {
                     this.gameMode.startDestroyBlock(var2x, var1.getDirection());
                     break;
                  }
               case MISS:
                  if (this.gameMode.hasMissTime()) {
                     this.missTime = 10;
                  }

                  this.player.resetAttackStrengthTicker();
            }

            this.player.swing(InteractionHand.MAIN_HAND);
         }
      }
   }

   private void startUseItem() {
      if (!this.gameMode.isDestroying()) {
         this.rightClickDelay = 4;
         if (!this.player.isHandsBusy()) {
            if (this.hitResult == null) {
               LOGGER.warn("Null returned as 'hitResult', this shouldn't happen!");
            }

            for(InteractionHand var4 : InteractionHand.values()) {
               ItemStack var5 = this.player.getItemInHand(var4);
               if (this.hitResult != null) {
                  switch(this.hitResult.getType()) {
                     case ENTITY:
                        EntityHitResult var6x = (EntityHitResult)this.hitResult;
                        Entity var7xx = var6x.getEntity();
                        InteractionResult var8xxx = this.gameMode.interactAt(this.player, var7xx, var6x,var4);
                        if (!var8xxx.consumesAction()) {
                           var8xxx = this.gameMode.interact(this.player, var7xx, var4);
                        }

                        if (var8xxx.consumesAction()) {
                           if (var8xxx.shouldSwing()) {
                              this.player.swing(var4);
                           }

                           return;
                        }
                        break;
                     case BLOCK:
                        BlockHitResult var9x = (BlockHitResult)this.hitResult;
                        int var10xx = var5.getCount();
                        InteractionResult var11xxx = this.gameMode.useItemOn(this.player, this.level, var4, var9x);
                        if (var11xxx.consumesAction()) {
                           if (var11xxx.shouldSwing()) {
                              this.player.swing(var4);
                              if (!var5.isEmpty() && (var5.getCount() != var10xx || this.gameMode.hasInfiniteItems())) {
                                 this.gameRenderer.itemInHandRenderer.itemUsed(var4);
                              }
                           }

                           return;
                        }

                        if (var11xxx == InteractionResult.FAIL) {
                           return;
                        }
                  }
               }

               if (!var5.isEmpty()) {
                  InteractionResult var12 = this.gameMode.useItem(this.player, this.level, var4);
                  if (var12.consumesAction()) {
                     if (var12.shouldSwing()) {
                        this.player.swing(var4);
                     }

                     this.gameRenderer.itemInHandRenderer.itemUsed(var4);
                     return;
                  }
               }
            }
         }
      }
   }

   public MusicManager getMusicManager() {
      return this.musicManager;
   }

   public void tick() {
      if (this.rightClickDelay > 0) {
         --this.rightClickDelay;
      }

      this.profiler.push("gui");
      if (!this.pause) {
         this.gui.tick();
      }

      this.profiler.pop();
      this.gameRenderer.pick(1.0F);
      this.tutorial.onLookAt(this.level, this.hitResult);
      this.profiler.push("gameMode");
      if (!this.pause && this.level != null) {
         this.gameMode.tick();
      }

      this.profiler.popPush("textures");
      if (this.level != null) {
         this.textureManager.tick();
      }

      if (this.screen == null && this.player != null) {
         if (this.player.isDeadOrDying() && !(this.screen instanceof DeathScreen)) {
            this.setScreen(null);
         } else if (this.player.isSleeping() && this.level != null) {
            this.setScreen(new InBedChatScreen());
         }
      } else if (this.screen != null && this.screen instanceof InBedChatScreen && !this.player.isSleeping()) {
         this.setScreen(null);
      }

      if (this.screen != null) {
         this.missTime = 10000;
      }

      if (this.screen != null) {
         Screen.wrapScreenError(() -> this.screen.tick(), "Ticking screen", this.screen.getClass().getCanonicalName());
      }

      if (!this.options.renderDebug) {
         this.gui.clearCache();
      }

      if (this.overlay == null && (this.screen == null || this.screen.passEvents)) {
         this.profiler.popPush("Keybindings");
         this.handleKeybinds();
         if (this.missTime > 0) {
            --this.missTime;
         }
      }

      if (this.level != null) {
         this.profiler.popPush("gameRenderer");
         if (!this.pause) {
            this.gameRenderer.tick();
         }

         this.profiler.popPush("levelRenderer");
         if (!this.pause) {
            this.levelRenderer.tick();
         }

         this.profiler.popPush("level");
         if (!this.pause) {
            if (this.level.getSkyFlashTime() > 0) {
               this.level.setSkyFlashTime(this.level.getSkyFlashTime() - 1);
            }

            this.level.tickEntities();
         }
      } else if (this.gameRenderer.currentEffect() != null) {
         this.gameRenderer.shutdownEffect();
      }

      if (!this.pause) {
         this.musicManager.tick();
      }

      this.soundManager.tick(this.pause);
      if (this.level != null) {
         if (!this.pause) {
            if (!this.options.joinedFirstServer && this.isMultiplayerServer()) {
               Component var1 = new TranslatableComponent("tutorial.socialInteractions.title");
               Component var2x = new TranslatableComponent("tutorial.socialInteractions.description", Tutorial.key("socialInteractions"));
               this.socialInteractionsToast = new TutorialToast(TutorialToast.Icons.SOCIAL_INTERACTIONS, var1, var2x, true);
               this.tutorial.addTimedToast(this.socialInteractionsToast, 160);
               this.options.joinedFirstServer = true;
               this.options.save();
            }

            this.tutorial.tick();

            try {
               this.level.tick(() -> true);
            } catch (Throwable var4) {
               CrashReport var5 = CrashReport.forThrowable(var4, "Exception in world tick");
               if (this.level == null) {
                  CrashReportCategory var6x = var5.addCategory("Affected level");
                  var6x.setDetail("Problem", "Level is null!");
               } else {
                  this.level.fillReportDetails(var5);
               }

               throw new ReportedException(var5);
            }
         }

         this.profiler.popPush("animateTick");
         if (!this.pause && this.level != null) {
            this.level.animateTick(Mth.floor(this.player.getX()), Mth.floor(this.player.getY()), Mth.floor(this.player.getZ()));
         }

         this.profiler.popPush("particles");
         if (!this.pause) {
            this.particleEngine.tick();
         }
      } else if (this.pendingConnection != null) {
         this.profiler.popPush("pendingConnection");
         this.pendingConnection.tick();
      }

      this.profiler.popPush("keyboard");
      this.keyboardHandler.tick();
      this.profiler.pop();
   }

   private boolean isMultiplayerServer() {
      return !this.isLocalServer || this.singleplayerServer != null && this.singleplayerServer.isPublished();
   }

   private void handleKeybinds() {
      for(; this.options.keyTogglePerspective.consumeClick(); this.levelRenderer.needsUpdate()) {
         CameraType var1 = this.options.getCameraType();
         this.options.setCameraType(this.options.getCameraType().cycle());
         if (var1.isFirstPerson() != this.options.getCameraType().isFirstPerson()) {
            this.gameRenderer.checkEntityPostEffect(this.options.getCameraType().isFirstPerson() ? this.getCameraEntity() : null);
         }
      }

      while(this.options.keySmoothCamera.consumeClick()) {
         this.options.smoothCamera = !this.options.smoothCamera;
      }

      for(int var4 = 0; var4 < 9; ++var4) {
         boolean var2x = this.options.keySaveHotbarActivator.isDown();
         boolean var3xx = this.options.keyLoadHotbarActivator.isDown();
         if (this.options.keyHotbarSlots[var4].consumeClick()) {
            if (this.player.isSpectator()) {
               this.gui.getSpectatorGui().onHotbarSelected(var4);
            } else if (!this.player.isCreative() || this.screen != null || !var3xx && !var2x) {
               this.player.inventory.selected = var4;
            } else {
               CreativeModeInventoryScreen.handleHotbarLoadOrSave(this, var4, var3xx, var2x);
            }
         }
      }

      while(this.options.keySocialInteractions.consumeClick()) {
         if (!this.isMultiplayerServer()) {
            this.player.displayClientMessage(SOCIAL_INTERACTIONS_NOT_AVAILABLE, true);
            NarratorChatListener.INSTANCE.sayNow(SOCIAL_INTERACTIONS_NOT_AVAILABLE.getString());
         } else {
            if (this.socialInteractionsToast != null) {
               this.tutorial.removeTimedToast(this.socialInteractionsToast);
               this.socialInteractionsToast = null;
            }

            this.setScreen(new SocialInteractionsScreen());
         }
      }

      while(this.options.keyInventory.consumeClick()) {
         if (this.gameMode.isServerControlledInventory()) {
            this.player.sendOpenInventory();
         } else {
            this.tutorial.onOpenInventory();
            this.setScreen(new InventoryScreen(this.player));
         }
      }

      while(this.options.keyAdvancements.consumeClick()) {
         this.setScreen(new AdvancementsScreen(this.player.connection.getAdvancements()));
      }

      while(this.options.keySwapOffhand.consumeClick()) {
         if (!this.player.isSpectator()) {
            this.getConnection()
                    .send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN));
         }
      }

      while(this.options.keyDrop.consumeClick()) {
         if (!this.player.isSpectator() && this.player.drop(Screen.hasControlDown())) {
            this.player.swing(InteractionHand.MAIN_HAND);
         }
      }

      boolean var5 = this.options.chatVisibility != ChatVisiblity.HIDDEN;
      if (var5) {
         while(this.options.keyChat.consumeClick()) {
            this.openChatScreen("");
         }

         if (this.screen == null && this.overlay == null && this.options.keyCommand.consumeClick()) {
            this.openChatScreen("/");
         }
      }

      if (this.player.isUsingItem()) {
         if (!this.options.keyUse.isDown()) {
            this.gameMode.releaseUsingItem(this.player);
         }

         while(this.options.keyAttack.consumeClick()) {
         }

         while(this.options.keyUse.consumeClick()) {
         }

         while(this.options.keyPickItem.consumeClick()) {
         }
      } else {
         while(this.options.keyAttack.consumeClick()) {
            this.startAttack();
         }

         while(this.options.keyUse.consumeClick()) {
            this.startUseItem();
         }

         while(this.options.keyPickItem.consumeClick()) {
            this.pickBlock();
         }
      }

      if (this.options.keyUse.isDown() && this.rightClickDelay == 0 && !this.player.isUsingItem()) {
         this.startUseItem();
      }

      this.continueAttack(this.screen == null && this.options.keyAttack.isDown() && this.mouseHandler.isMouseGrabbed());
   }

   public static DataPackConfig loadDataPacks(LevelStorageSource.LevelStorageAccess var0) {
      MinecraftServer.convertFromRegionFormatIfNeeded(var0);
      DataPackConfig var1 = var0.getDataPacks();
      if (var1 == null) {
         throw new IllegalStateException("Failed to load data pack config");
      } else {
         return var1;
      }
   }

   public static WorldData loadWorldData(
           LevelStorageSource.LevelStorageAccess var0, RegistryAccess.RegistryHolder var1, ResourceManager var2, DataPackConfig var3
   ) {
      RegistryReadOps<Tag> var4 = RegistryReadOps.create(NbtOps.INSTANCE, var2, var1);
      WorldData var5x = var0.getDataTag(var4, var3);
      if (var5x == null) {
         throw new IllegalStateException("Failed to load world");
      } else {
         return var5x;
      }
   }

   public void loadLevel(String var1) {
      this.doLoadLevel(var1, RegistryAccess.builtin(), Minecraft::loadDataPacks, Minecraft::loadWorldData, false, Minecraft.ExperimentalDialogType.BACKUP);
   }

   public void createLevel(String var1, LevelSettings var2, RegistryAccess.RegistryHolder var3, WorldGenSettings var4) {
      this.doLoadLevel(var1, var3,
              var1x -> var2.getDataPackConfig(),
              (var3x, var4x, var5, var6) -> {
                 RegistryWriteOps<JsonElement> var7 = RegistryWriteOps.create(JsonOps.INSTANCE, var3);
                 RegistryReadOps<JsonElement> var8x = RegistryReadOps.create(JsonOps.INSTANCE, var5, var3);
                 DataResult<WorldGenSettings> var9xx = WorldGenSettings.CODEC
                         .encodeStart(var7, var4)
                         .setLifecycle(Lifecycle.stable())
                         .flatMap(var1xx -> WorldGenSettings.CODEC.parse(var8x, var1xx));
                 WorldGenSettings var10xxx = var9xx.resultOrPartial(Util.prefix("Error reading worldgen settings after loading data packs: ", LOGGER::error)).orElse(var4);
                 return new PrimaryLevelData(var2, var10xxx, var9xx.lifecycle());
              },
              false,
              Minecraft.ExperimentalDialogType.CREATE
      );
   }

   private void doLoadLevel(
           String var1,
           RegistryAccess.RegistryHolder var2,
           Function<LevelStorageSource.LevelStorageAccess, DataPackConfig> var3,
           Function4<LevelStorageSource.LevelStorageAccess, RegistryAccess.RegistryHolder, ResourceManager, DataPackConfig, WorldData> var4,
           boolean var5,
           Minecraft.ExperimentalDialogType var6
   ) {
      LevelStorageSource.LevelStorageAccess var7;
      try {
         var7 = this.levelSource.createAccess(var1);
      } catch (IOException var21) {
         LOGGER.warn("Failed to read level {} data", var1, var21);
         SystemToast.onWorldAccessFailure(this, var1);
         this.setScreen(null);
         return;
      }

      Minecraft.ServerStem var8;
      try {
         var8 = this.makeServerStem(var2, var3, var4, var5, var7);
      } catch (Exception var20) {
         LOGGER.warn("Failed to load datapacks, can't proceed with server load", var20);
         this.setScreen(new DatapackLoadFailureScreen(() -> this.doLoadLevel(var1, var2, var3, var4, true, var6)));

         try {
            var7.close();
         } catch (IOException var16) {
            LOGGER.warn("Failed to unlock access to level {}", var1, var16);
         }

         return;
      }

      WorldData var9 = var8.worldData();
      boolean var10x = var9.worldGenSettings().isOldCustomizedWorld();
      boolean var11xx = var9.worldGenSettingsLifecycle() != Lifecycle.stable();
      if (var6 == Minecraft.ExperimentalDialogType.NONE || !var10x && !var11xx) {
         this.clearLevel();
         this.progressListener.set(null);

         try {
            var7.saveDataTag(var2, var9);
            var8.serverResources().updateGlobals();
            YggdrasilAuthenticationService var12xxx = new YggdrasilAuthenticationService(this.proxy);
            MinecraftSessionService var23xxxx = var12xxx.createMinecraftSessionService();
            GameProfileRepository var25xxxxx = var12xxx.createProfileRepository();
            GameProfileCache var15xxxxxx = new GameProfileCache(var25xxxxx, new File(this.gameDirectory, MinecraftServer.USERID_CACHE_FILE.getName()));
            SkullBlockEntity.setProfileCache(var15xxxxxx);
            SkullBlockEntity.setSessionService(var23xxxx);
            GameProfileCache.setUsesAuthentication(false);
            this.singleplayerServer = MinecraftServer.spin(
                    var8x -> new IntegratedServer(var8x, this, var2, var7, var8.packRepository(), var8.serverResources(), var9, var23xxxx, var25xxxxx, var15xxxxxx, var1xx -> {
                       StoringChunkProgressListener var2xx = new StoringChunkProgressListener(var1xx + 0);
                       var2xx.start();
                       this.progressListener.set(var2xx);
                       return new ProcessorChunkProgressListener(var2xx, this.progressTasks::add);
                    })
            );
            this.isLocalServer = true;
         } catch (Throwable var19) {
            CrashReport var13xxxxxxx = CrashReport.forThrowable(var19, "Starting integrated server");
            CrashReportCategory var14xxxxxxxx = var13xxxxxxx.addCategory("Starting integrated server");
            var14xxxxxxxx.setDetail("Level ID", var1);
            var14xxxxxxxx.setDetail("Level Name", var9.getLevelName());
            throw new ReportedException(var13xxxxxxx);
         }

         while(this.progressListener.get() == null) {
            Thread.yield();
         }

         LevelLoadingScreen var22xxx = new LevelLoadingScreen(this.progressListener.get());
         this.setScreen(var22xxx);
         this.profiler.push("waitForServer");

         while(!this.singleplayerServer.isReady()) {
            var22xxx.tick();
            this.runTick(false);

            try {
               Thread.sleep(16L);
            } catch (InterruptedException var18) {
            }

            if (this.delayedCrash != null) {
               crash(this.delayedCrash);
               return;
            }
         }

         this.profiler.pop();
         SocketAddress var24xxxx = this.singleplayerServer.getConnection().startMemoryChannel();
         Connection var26xxxxx = Connection.connectToLocalServer(var24xxxx);
         var26xxxxx.setListener(new ClientHandshakePacketListenerImpl(var26xxxxx, this, null, var0 -> {
         }));
         var26xxxxx.send(new ClientIntentionPacket(var24xxxx.toString(), 0, ConnectionProtocol.LOGIN));
         var26xxxxx.send(new ServerboundHelloPacket(this.getUser().getGameProfile()));
         this.pendingConnection = var26xxxxx;
      } else {
         this.displayExperimentalConfirmationDialog(var6, var1, var10x, () -> this.doLoadLevel(var1, var2, var3, var4, var5, Minecraft.ExperimentalDialogType.NONE));
         var8.close();

         try {
            var7.close();
         } catch (IOException var17) {
            LOGGER.warn("Failed to unlock access to level {}", var1, var17);
         }
      }
   }

   private void displayExperimentalConfirmationDialog(Minecraft.ExperimentalDialogType var1, String var2, boolean var3, Runnable var4) {
      if (var1 == Minecraft.ExperimentalDialogType.BACKUP) {
         Component var5;
         Component var6x;
         if (var3) {
            var5 = new TranslatableComponent("selectWorld.backupQuestion.customized");
            var6x = new TranslatableComponent("selectWorld.backupWarning.customized");
         } else {
            var5 = new TranslatableComponent("selectWorld.backupQuestion.experimental");
            var6x = new TranslatableComponent("selectWorld.backupWarning.experimental");
         }

         this.setScreen(new BackupConfirmScreen(null, (var3x, var4x) -> {
            if (var3x) {
               EditWorldScreen.makeBackupAndShowToast(this.levelSource,var2);
            }

            var4.run();
         }, var5, var6x, false));
      } else {
         this.setScreen(
                 new ConfirmScreen(
                         var3x -> {
                            if (var3x) {
                               var4.run();
                            } else {
                               this.setScreen(null);

                               try (LevelStorageSource.LevelStorageAccess var4x = this.levelSource.createAccess(var2)) {
                                  var4x.deleteLevel();
                               } catch (IOException var17) {
                                  SystemToast.onWorldDeleteFailure(this, var2);
                                  LOGGER.error("Failed to delete world {}", var2, var17);
                               }
                            }
                         },
                         new TranslatableComponent("selectWorld.backupQuestion.experimental"),
                         new TranslatableComponent("selectWorld.backupWarning.experimental"),
                         CommonComponents.GUI_PROCEED,
                         CommonComponents.GUI_CANCEL
                 )
         );
      }
   }

   public Minecraft.ServerStem makeServerStem(
           RegistryAccess.RegistryHolder var1,
           Function<LevelStorageSource.LevelStorageAccess, DataPackConfig> var2,
           Function4<LevelStorageSource.LevelStorageAccess, RegistryAccess.RegistryHolder, ResourceManager, DataPackConfig, WorldData> var3,
           boolean var4,
           LevelStorageSource.LevelStorageAccess var5
   ) throws InterruptedException, ExecutionException {
      DataPackConfig var6 = var2.apply(var5);
      PackRepository var7x = new PackRepository(
              new ServerPacksSource(), new FolderRepositorySource(var5.getLevelPath(LevelResource.DATAPACK_DIR).toFile(), PackSource.WORLD)
      );

      try {
         DataPackConfig var8xx = MinecraftServer.configurePackRepository(var7x, var6, var4);
         CompletableFuture<ServerResources> var9xxx = ServerResources.loadResources(
                 var7x.openAllSelected(), Commands.CommandSelection.INTEGRATED, 2, Util.backgroundExecutor(), this
         );
         this.managedBlock(var9xxx::isDone);
         ServerResources var10xxxx = var9xxx.get();
         WorldData var11xxxxx = var3.apply(var5, var1, var10xxxx.getResourceManager(), var8xx);
         return new Minecraft.ServerStem(var7x, var10xxxx, var11xxxxx);
      } catch (ExecutionException | InterruptedException var12) {
         var7x.close();
         throw var12;
      }
   }

   public void setLevel(ClientLevel var1) {
      ProgressScreen var2 = new ProgressScreen();
      var2.progressStartNoAbort(new TranslatableComponent("connect.joining"));
      this.updateScreenAndTick(var2);
      this.level = var1;
      this.updateLevelInEngines(var1);
      if (!this.isLocalServer) {
         AuthenticationService var3x = new YggdrasilAuthenticationService(this.proxy);
         MinecraftSessionService var4xx = var3x.createMinecraftSessionService();
         GameProfileRepository var5xxx = var3x.createProfileRepository();
         GameProfileCache var6xxxx = new GameProfileCache(var5xxx, new File(this.gameDirectory, MinecraftServer.USERID_CACHE_FILE.getName()));
         SkullBlockEntity.setProfileCache(var6xxxx);
         SkullBlockEntity.setSessionService(var4xx);
         GameProfileCache.setUsesAuthentication(false);
      }
   }

   public void clearLevel() {
      this.clearLevel(new ProgressScreen());
   }

   public void clearLevel(Screen var1) {
      ClientPacketListener var2 = this.getConnection();
      if (var2 != null) {
         this.dropAllTasks();
         var2.cleanup();
      }

      IntegratedServer var3 = this.singleplayerServer;
      this.singleplayerServer = null;
      this.gameRenderer.resetData();
      this.gameMode = null;
      NarratorChatListener.INSTANCE.clear();
      this.updateScreenAndTick(var1);
      if (this.level != null) {
         if (var3 != null) {
            this.profiler.push("waitForServer");

            while(!var3.isShutdown()) {
               this.runTick(false);
            }

            this.profiler.pop();
         }

         this.clientPackSource.clearServerPack();
         this.gui.onDisconnected();
         this.currentServer = null;
         this.isLocalServer = false;
         this.game.onLeaveGameSession();
      }

      this.level = null;
      this.updateLevelInEngines(null);
      this.player = null;
   }

   private void updateScreenAndTick(Screen var1) {
      this.profiler.push("forcedTick");
      this.soundManager.stop();
      this.cameraEntity = null;
      this.pendingConnection = null;
      this.setScreen(var1);
      this.runTick(false);
      this.profiler.pop();
   }

   public void forceSetScreen(Screen var1) {
      this.profiler.push("forcedTick");
      this.setScreen(var1);
      this.runTick(false);
      this.profiler.pop();
   }

   private void updateLevelInEngines(@Nullable ClientLevel var1) {
      this.levelRenderer.setLevel(var1);
      this.particleEngine.setLevel(var1);
      BlockEntityRenderDispatcher.instance.setLevel(var1);
      this.updateTitle();
   }

   public boolean allowsMultiplayer() {
      return this.allowsMultiplayer && this.socialInteractionsService.serversAllowed();
   }

   public boolean isBlocked(UUID var1) {
      if (this.allowsChat()) {
         return this.playerSocialManager.shouldHideMessageFrom(var1);
      } else {
         return (this.player == null || !var1.equals(this.player.getUUID())) && !var1.equals(Util.NIL_UUID);
      }
   }

   public boolean allowsChat() {
      return this.allowsChat && this.socialInteractionsService.chatAllowed();
   }

   public final boolean isDemo() {
      return this.demo;
   }

   @Nullable
   public ClientPacketListener getConnection() {
      return this.player == null ? null : this.player.connection;
   }

   public static boolean renderNames() {
      return !instance.options.hideGui;
   }

   public static boolean useFancyGraphics() {
      return instance.options.graphicsMode.getId() >= GraphicsStatus.FANCY.getId();
   }

   public static boolean useShaderTransparency() {
      return instance.options.graphicsMode.getId() >= GraphicsStatus.FABULOUS.getId();
   }

   public static boolean useAmbientOcclusion() {
      return instance.options.ambientOcclusion != AmbientOcclusionStatus.OFF;
   }

   private void pickBlock() {
      if (this.hitResult != null && this.hitResult.getType() != HitResult.Type.MISS) {
         boolean var1x = this.player.abilities.instabuild;
         BlockEntity var2xx = null;
         HitResult.Type var4xxx = this.hitResult.getType();
         ItemStack var3;
         if (var4xxx == HitResult.Type.BLOCK) {
            BlockPos var5xxxx = ((BlockHitResult)this.hitResult).getBlockPos();
            BlockState var6xxxxx = this.level.getBlockState(var5xxxx);
            Block var7xxxxxx = var6xxxxx.getBlock();
            if (var6xxxxx.isAir()) {
               return;
            }

            var3 = var7xxxxxx.getCloneItemStack(this.level, var5xxxx, var6xxxxx);
            if (var3.isEmpty()) {
               return;
            }

            if (var1x && Screen.hasControlDown() && var7xxxxxx.isEntityBlock()) {
               var2xx = this.level.getBlockEntity(var5xxxx);
            }
         } else {
            if (var4xxx != HitResult.Type.ENTITY || !var1x) {
               return;
            }

            Entity var8 = ((EntityHitResult)this.hitResult).getEntity();
            if (var8 instanceof Painting) {
               var3 = new ItemStack(Items.PAINTING);
            } else if (var8 instanceof LeashFenceKnotEntity) {
               var3 = new ItemStack(Items.LEAD);
            } else if (var8 instanceof ItemFrame) {
               ItemFrame var11 = (ItemFrame)var8;
               ItemStack var15x = var11.getItem();
               if (var15x.isEmpty()) {
                  var3 = new ItemStack(Items.ITEM_FRAME);
               } else {
                  var3 = var15x.copy();
               }
            } else if (var8 instanceof AbstractMinecart) {
               AbstractMinecart var12x = (AbstractMinecart)var8;
               Item var16;
               switch(var12x.getMinecartType()) {
                  case FURNACE:
                     var16 = Items.FURNACE_MINECART;
                     break;
                  case CHEST:
                     var16 = Items.CHEST_MINECART;
                     break;
                  case TNT:
                     var16 = Items.TNT_MINECART;
                     break;
                  case HOPPER:
                     var16 = Items.HOPPER_MINECART;
                     break;
                  case COMMAND_BLOCK:
                     var16 = Items.COMMAND_BLOCK_MINECART;
                     break;
                  default:
                     var16 = Items.MINECART;
               }

               var3 = new ItemStack(var16);
            } else if (var8 instanceof Boat) {
               var3 = new ItemStack(((Boat)var8).getDropItem());
            } else if (var8 instanceof ArmorStand) {
               var3 = new ItemStack(Items.ARMOR_STAND);
            } else if (var8 instanceof EndCrystal) {
               var3 = new ItemStack(Items.END_CRYSTAL);
            } else {
               SpawnEggItem var13 = SpawnEggItem.byId(var8.getType());
               if (var13 == null) {
                  return;
               }

               var3 = new ItemStack(var13);
            }
         }

         if (var3.isEmpty()) {
            String var10 = "";
            if (var4xxx == HitResult.Type.BLOCK) {
               var10 = Registry.BLOCK.getKey(this.level.getBlockState(((BlockHitResult)this.hitResult).getBlockPos()).getBlock()).toString();
            } else if (var4xxx == HitResult.Type.ENTITY) {
               var10 = Registry.ENTITY_TYPE.getKey(((EntityHitResult)this.hitResult).getEntity().getType()).toString();
            }

            LOGGER.warn("Picking on: [{}] {} gave null item", var4xxx, var10);
         } else {
            Inventory var9 = this.player.inventory;
            if (var2xx != null) {
               this.addCustomNbtData(var3, var2xx);
            }

            int var14 = var9.findSlotMatchingItem(var3);
            if (var1x) {
               var9.setPickedItem(var3);
               this.gameMode.handleCreativeModeItemAdd(this.player.getItemInHand(InteractionHand.MAIN_HAND), 36 + var9.selected);
            } else if (var14 != -1) {
               if (Inventory.isHotbarSlot(var14)) {
                  var9.selected = var14;
               } else {
                  this.gameMode.handlePickItem(var14);
               }
            }
         }
      }
   }

   private ItemStack addCustomNbtData(ItemStack var1, BlockEntity var2) {
      CompoundTag var3 = var2.save(new CompoundTag());
      if (var1.getItem() instanceof PlayerHeadItem && var3.contains("SkullOwner")) {
         CompoundTag var6x = var3.getCompound("SkullOwner");
         var1.getOrCreateTag().put("SkullOwner", var6x);
         return var1;
      } else {
         var1.addTagElement("BlockEntityTag", var3);
         CompoundTag var4 = new CompoundTag();
         ListTag var5x = new ListTag();
         var5x.add(StringTag.valueOf("\"(+NBT)\""));
         var4.put("Lore", var5x);
         var1.addTagElement("display", var4);
         return var1;
      }
   }

   public CrashReport fillReport(CrashReport var1) {
      fillReport(this.languageManager, this.launchedVersion, this.options, var1);
      if (this.level != null) {
         this.level.fillReportDetails(var1);
      }

      return var1;
   }

   public static void fillReport(@Nullable LanguageManager var0, String var1, @Nullable Options var2, CrashReport var3) {
      CrashReportCategory var4 = var3.getSystemDetails();
      var4.setDetail("Launched Version", () -> var1);
      var4.setDetail("Backend library", RenderSystem::getBackendDescription);
      var4.setDetail("Backend API", RenderSystem::getApiDescription);
      var4.setDetail("GL Caps", RenderSystem::getCapsString);
      var4.setDetail("Using VBOs", () -> "Yes");
      var4.setDetail(
              "Is Modded",
              () -> {
                 String var0x = ClientBrandRetriever.getClientModName();
                 if (!"vanilla".equals(var0x)) {
                    return "Definitely; Client brand changed to '" + var0x + "'";
                 } else {
                    return Minecraft.class.getSigners() == null
                            ? "Very likely; Jar signature invalidated"
                            : "Probably not. Jar signature remains and client brand is untouched.";
                 }
              }
      );
      var4.setDetail("Type", "Client (map_client.txt)");
      if (var2 != null) {
         if (instance != null) {
            String var5x = instance.getGpuWarnlistManager().getAllWarnings();
            if (var5x != null) {
               var4.setDetail("GPU Warnings", var5x);
            }
         }

         var4.setDetail("Graphics mode", var2.graphicsMode);
         var4.setDetail("Resource Packs", () -> {
            StringBuilder var1x = new StringBuilder();

            for(String var3x : var2.resourcePacks) {
               if (var1x.length() > 0) {
                  var1x.append(", ");
               }

               var1x.append(var3x);
               if (var2.incompatibleResourcePacks.contains(var3x)) {
                  var1x.append(" (incompatible)");
               }
            }

            return var1x.toString();
         });
      }

      if (var0 != null) {
         var4.setDetail("Current Language", () -> var0.getSelected().toString());
      }

      var4.setDetail("CPU", GlUtil::getCpuInfo);
   }

   public static Minecraft getInstance() {
      return instance;
   }

   public CompletableFuture<Void> delayTextureReload() {
      return this.submit(this::reloadResourcePacks).thenCompose(var0 -> var0);
   }

   @Override
   public void populateSnooper(Snooper var1) {
      var1.setDynamicData("fps", fps);
      var1.setDynamicData("vsync_enabled", this.options.enableVsync);
      var1.setDynamicData("display_frequency", this.window.getRefreshRate());
      var1.setDynamicData("display_type", this.window.isFullscreen() ? "fullscreen" : "windowed");
      var1.setDynamicData("run_time", (Util.getMillis() - var1.getStartupTime()) / 60L * 1000L);
      var1.setDynamicData("current_action", this.getCurrentSnooperAction());
      var1.setDynamicData("language", this.options.languageCode == null ? "en_us" : this.options.languageCode);
      String var2 = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN ? "little" : "big";
      var1.setDynamicData("endianness", var2);
      var1.setDynamicData("subtitles", this.options.showSubtitles);
      var1.setDynamicData("touch", this.options.touchscreen ? "touch" : "mouse");
      int var3x = 0;

      for(Pack var5 : this.resourcePackRepository.getSelectedPacks()) {
         if (!var5.isRequired() && !var5.isFixedPosition()) {
            var1.setDynamicData("resource_pack[" + var3x++ + "]", var5.getId());
         }
      }

      var1.setDynamicData("resource_packs", var3x);
      if (this.singleplayerServer != null) {
         var1.setDynamicData("snooper_partner", this.singleplayerServer.getSnooper().getToken());
      }
   }

   private String getCurrentSnooperAction() {
      if (this.singleplayerServer != null) {
         return this.singleplayerServer.isPublished() ? "hosting_lan" : "singleplayer";
      } else if (this.currentServer != null) {
         return this.currentServer.isLan() ? "playing_lan" : "multiplayer";
      } else {
         return "out_of_game";
      }
   }

   public void setCurrentServer(@Nullable ServerData var1) {
      this.currentServer = var1;
   }

   @Nullable
   public ServerData getCurrentServer() {
      return this.currentServer;
   }

   public boolean isLocalServer() {
      return this.isLocalServer;
   }

   public boolean hasSingleplayerServer() {
      return this.isLocalServer && this.singleplayerServer != null;
   }

   @Nullable
   public IntegratedServer getSingleplayerServer() {
      return this.singleplayerServer;
   }

   public Snooper getSnooper() {
      return this.snooper;
   }

   public User getUser() {
      return this.user;
   }

   public PropertyMap getProfileProperties() {
      if (this.profileProperties.isEmpty()) {
         GameProfile var1 = this.getMinecraftSessionService().fillProfileProperties(this.user.getGameProfile(), false);
         this.profileProperties.putAll(var1.getProperties());
      }

      return this.profileProperties;
   }

   public Proxy getProxy() {
      return this.proxy;
   }

   public TextureManager getTextureManager() {
      return this.textureManager;
   }

   public ResourceManager getResourceManager() {
      return this.resourceManager;
   }

   public PackRepository getResourcePackRepository() {
      return this.resourcePackRepository;
   }

   public ClientPackSource getClientPackSource() {
      return this.clientPackSource;
   }

   public File getResourcePackDirectory() {
      return this.resourcePackDirectory;
   }

   public LanguageManager getLanguageManager() {
      return this.languageManager;
   }

   public Function<ResourceLocation, TextureAtlasSprite> getTextureAtlas(ResourceLocation var1) {
      return this.modelManager.getAtlas(var1)::getSprite;
   }

   public boolean is64Bit() {
      return this.is64bit;
   }

   public boolean isPaused() {
      return this.pause;
   }

   public GpuWarnlistManager getGpuWarnlistManager() {
      return this.gpuWarnlistManager;
   }

   public SoundManager getSoundManager() {
      return this.soundManager;
   }

   public Music getSituationalMusic() {
      if (this.screen instanceof WinScreen) {
         return Musics.CREDITS;
      } else if (this.player != null) {
         if (this.player.level.dimension() == Level.END) {
            return this.gui.getBossOverlay().shouldPlayMusic() ? Musics.END_BOSS : Musics.END;
         } else {
            Biome.BiomeCategory var1 = this.player.level.getBiome(this.player.blockPosition()).getBiomeCategory();
            if (!this.musicManager.isPlayingMusic(Musics.UNDER_WATER)
                    && (!this.player.isUnderWater() || var1 != Biome.BiomeCategory.OCEAN && var1 != Biome.BiomeCategory.RIVER)) {
               return this.player.level.dimension() != Level.NETHER && this.player.abilities.instabuild && this.player.abilities.mayfly
                       ? Musics.CREATIVE
                       : this.level.getBiomeManager().getNoiseBiomeAtPosition(this.player.blockPosition()).getBackgroundMusic().orElse(Musics.GAME);
            } else {
               return Musics.UNDER_WATER;
            }
         }
      } else {
         return Musics.MENU;
      }
   }

   public MinecraftSessionService getMinecraftSessionService() {
      return this.minecraftSessionService;
   }

   public SkinManager getSkinManager() {
      return this.skinManager;
   }

   @Nullable
   public Entity getCameraEntity() {
      return this.cameraEntity;
   }

   public void setCameraEntity(Entity var1) {
      this.cameraEntity = var1;
      this.gameRenderer.checkEntityPostEffect(var1);
   }

   public boolean shouldEntityAppearGlowing(Entity var1) {
      return var1.isGlowing()
              || this.player != null && this.player.isSpectator() && this.options.keySpectatorOutlines.isDown() && var1.getType() == EntityType.PLAYER;
   }

   @Override
   protected Thread getRunningThread() {
      return this.gameThread;
   }

   @Override
   protected Runnable wrapRunnable(Runnable var1) {
      return var1;
   }

   @Override
   protected boolean shouldRun(Runnable var1) {
      return true;
   }

   public BlockRenderDispatcher getBlockRenderer() {
      return this.blockRenderer;
   }

   public EntityRenderDispatcher getEntityRenderDispatcher() {
      return this.entityRenderDispatcher;
   }

   public ItemRenderer getItemRenderer() {
      return this.itemRenderer;
   }

   public ItemInHandRenderer getItemInHandRenderer() {
      return this.itemInHandRenderer;
   }

   public <T> MutableSearchTree<T> getSearchTree(SearchRegistry.Key<T> var1) {
      return this.searchRegistry.getTree(var1);
   }

   public FrameTimer getFrameTimer() {
      return this.frameTimer;
   }

   public boolean isConnectedToRealms() {
      return this.connectedToRealms;
   }

   public void setConnectedToRealms(boolean var1) {
      this.connectedToRealms = var1;
   }

   public DataFixer getFixerUpper() {
      return this.fixerUpper;
   }

   public float getFrameTime() {
      return this.timer.partialTick;
   }

   public float getDeltaFrameTime() {
      return this.timer.tickDelta;
   }

   public BlockColors getBlockColors() {
      return this.blockColors;
   }

   public boolean showOnlyReducedInfo() {
      return this.player != null && this.player.isReducedDebugInfo() || this.options.reducedDebugInfo;
   }

   public ToastComponent getToasts() {
      return this.toast;
   }

   public Tutorial getTutorial() {
      return this.tutorial;
   }

   public boolean isWindowActive() {
      return this.windowActive;
   }

   public HotbarManager getHotbarManager() {
      return this.hotbarManager;
   }

   public ModelManager getModelManager() {
      return this.modelManager;
   }

   public PaintingTextureManager getPaintingTextures() {
      return this.paintingTextures;
   }

   public MobEffectTextureManager getMobEffectTextures() {
      return this.mobEffectTextures;
   }

   @Override
   public void setWindowActive(boolean var1) {
      this.windowActive = var1;
   }

   public ProfilerFiller getProfiler() {
      return this.profiler;
   }

   public Game getGame() {
      return this.game;
   }

   public SplashManager getSplashManager() {
      return this.splashManager;
   }

   @Nullable
   public Overlay getOverlay() {
      return this.overlay;
   }

   public PlayerSocialManager getPlayerSocialManager() {
      return this.playerSocialManager;
   }

   public boolean renderOnThread() {
      return false;
   }

   public Window getWindow() {
      return this.window;
   }

   public RenderBuffers renderBuffers() {
      return this.renderBuffers;
   }

   private static Pack createClientPackAdapter(
           String var0, boolean var1, Supplier<PackResources> var2, PackResources var3, PackMetadataSection var4, Pack.Position var5, PackSource var6
   ) {
      int var7 = var4.getPackFormat();
      Supplier<PackResources> var8x = var2;
      if (var7 <= 3) {
         var8x = adaptV3(var2);
      }

      if (var7 <= 4) {
         var8x = adaptV4(var8x);
      }

      return new Pack(var0, var1, var8x, var3, var4, var5, var6);
   }

   private static Supplier<PackResources> adaptV3(Supplier<PackResources> var0) {
      return () -> new LegacyPackResourcesAdapter(var0.get(), LegacyPackResourcesAdapter.V3);
   }

   private static Supplier<PackResources> adaptV4(Supplier<PackResources> var0) {
      return () -> new PackResourcesAdapterV4(var0.get());
   }

   public void updateMaxMipLevel(int var1) {
      this.modelManager.updateMaxMipLevel(var1);
   }



   public static final class ServerStem implements AutoCloseable {
      private final PackRepository packRepository;
      private final ServerResources serverResources;
      private final WorldData worldData;

      private ServerStem(PackRepository var1, ServerResources var2, WorldData var3) {
         this.packRepository = var1;
         this.serverResources = var2;
         this.worldData = var3;
      }

      public PackRepository packRepository() {
         return this.packRepository;
      }

      public ServerResources serverResources() {
         return this.serverResources;
      }

      public WorldData worldData() {
         return this.worldData;
      }

      @Override
      public void close() {
         this.packRepository.close();
         this.serverResources.close();
      }
   }
    static enum ExperimentalDialogType {
        NONE,
        CREATE,
        BACKUP;
    }
}
