# ViperLevels - Piano di Sviluppo Completo

## Panoramica
Plugin avanzato di restrizioni mcMMO con GUI, database e logiche condizionali complesse.

---

## PARTE 1: Setup Base del Progetto
**Obiettivo**: Creare struttura base, dipendenze, plugin principale

### Passaggi:
1. Creare build.gradle con dipendenze mcMMO, ItemsAdder, XSeries, Lombok
2. Creare manifest.kod per librerie esterne (HikariCP per DB)
3. Creare plugin.yml con dipendenze hard/soft
4. Creare classe principale ViperLevels extends JavaPlugin
5. Setup logging e verifiche dipendenze onEnable

### File creati:
- build.gradle
- manifest.kod
- src/main/resources/plugin.yml
- src/main/java/com/viperlevels/ViperLevels.java

---

## PARTE 2: Sistema di Parsing Condizioni (Core Logic)
**Obiettivo**: Parser per condizioni tipo "MINING(50) AND (ACROBATICS(20) OR SWORDS(10))"

### Passaggi:
1. Creare enum McMMOSkill con tutte le 17 skill supportate
2. Creare classe SkillRequirement (skill + livello richiesto)
3. Creare classe Condition (singola condizione)
4. Creare classe ConditionParser con logica AND/OR
5. Creare ConditionEvaluator che verifica vs player mcMMO
6. Unit test logica parsing (commentati ma presenti)

### File creati:
- src/main/java/com/viperlevels/condition/McMMOSkill.java
- src/main/java/com/viperlevels/condition/SkillRequirement.java
- src/main/java/com/viperlevels/condition/Condition.java
- src/main/java/com/viperlevels/condition/ConditionParser.java
- src/main/java/com/viperlevels/condition/ConditionEvaluator.java

---

## PARTE 3: Config Manager e Struttura YML
**Obiettivo**: Gestire config.yml con settings, messages, penalties, rules

### Passaggi:
1. Creare config.yml di default con struttura completa
2. Creare ConfigManager singleton con reload support
3. Creare SettingsConfig per settings section
4. Creare MessagesConfig per messages section
5. Creare PenaltiesConfig per penalties section
6. Sistema di placeholder per messaggi (%requirements%, %skill%, ecc.)

### File creati:
- src/main/resources/config.yml
- src/main/java/com/viperlevels/config/ConfigManager.java
- src/main/java/com/viperlevels/config/SettingsConfig.java
- src/main/java/com/viperlevels/config/MessagesConfig.java
- src/main/java/com/viperlevels/config/PenaltiesConfig.java
- src/main/java/com/viperlevels/config/PlaceholderReplacer.java

---

## PARTE 4: Database Layer
**Obiettivo**: Supporto SQLite e MySQL per bypass, cache, statistiche

### Passaggi:
1. Creare DatabaseManager con factory pattern (SQLite/MySQL)
2. Creare tabelle: players_bypass, cached_levels, statistics
3. Implementare BypassRepository per gestire bypass player
4. Implementare CacheRepository per livelli mcMMO
5. Implementare StatsRepository per statistiche
6. Async queries con CompletableFuture
7. HikariCP connection pooling

### File creati:
- src/main/java/com/viperlevels/database/DatabaseManager.java
- src/main/java/com/viperlevels/database/DatabaseType.java
- src/main/java/com/viperlevels/database/SQLiteDatabase.java
- src/main/java/com/viperlevels/database/MySQLDatabase.java
- src/main/java/com/viperlevels/database/repository/BypassRepository.java
- src/main/java/com/viperlevels/database/repository/CacheRepository.java
- src/main/java/com/viperlevels/database/repository/StatsRepository.java

---

## PARTE 5: Sistema di Cache
**Obiettivo**: Cache TTL-based per skill mcMMO con invalidazione automatica

### Passaggi:
1. Creare CachedSkillData con timestamp + livelli
2. Creare CacheManager con Guava o HashMap + ScheduledExecutor
3. Cache per player UUID con TTL configurabile
4. Invalidazione manuale e automatica
5. Preload cache comuni
6. Metrics cache hit/miss

### File creati:
- src/main/java/com/viperlevels/cache/CacheManager.java
- src/main/java/com/viperlevels/cache/CachedSkillData.java
- src/main/java/com/viperlevels/cache/CacheService.java

---

## PARTE 6: Rule Manager e Validators
**Obiettivo**: Caricare regole da config e validare azioni player

### Passaggi:
1. Creare RuleType enum (ITEM, BLOCK, ARMOR, POTION, FOOD, MOB, DIMENSION)
2. Creare ActionType enum (BREAK, PLACE, CRAFT, USE, EQUIP, ENCHANT, CONSUME, HIT)
3. Creare Rule class (material/id + action + condition)
4. Creare MaterialGroup per regole globali
5. Creare RuleManager che carica da config e indicizza
6. Creare ValidationResult (pass/fail + missing requirements)
7. Creare RuleValidator che verifica player vs rule
8. Supporto ItemsAdder namespace

### File creati:
- src/main/java/com/viperlevels/rule/RuleType.java
- src/main/java/com/viperlevels/rule/ActionType.java
- src/main/java/com/viperlevels/rule/Rule.java
- src/main/java/com/viperlevels/rule/MaterialGroup.java
- src/main/java/com/viperlevels/rule/RuleManager.java
- src/main/java/com/viperlevels/rule/ValidationResult.java
- src/main/java/com/viperlevels/rule/RuleValidator.java

---

## PARTE 7: Event Listeners
**Obiettivo**: Intercettare eventi Bukkit e applicare restrizioni

### Passaggi:
1. BlockListener (BlockBreakEvent, BlockPlaceEvent)
2. ItemListener (PlayerInteractEvent per use item)
3. CraftListener (CraftItemEvent)
4. ArmorListener (InventoryClickEvent per equip)
5. CombatListener (EntityDamageByEntityEvent per hit mob)
6. ConsumeListener (PlayerItemConsumeEvent per food/potion)
7. DimensionListener (PlayerChangedWorldEvent)
8. EnchantListener (EnchantItemEvent, InventoryClickEvent per anvil)
9. Ogni listener usa RuleValidator + applica penalties
10. Invio messaggi personalizzati da MessagesConfig

### File creati:
- src/main/java/com/viperlevels/listener/BlockListener.java
- src/main/java/com/viperlevels/listener/ItemListener.java
- src/main/java/com/viperlevels/listener/CraftListener.java
- src/main/java/com/viperlevels/listener/ArmorListener.java
- src/main/java/com/viperlevels/listener/CombatListener.java
- src/main/java/com/viperlevels/listener/ConsumeListener.java
- src/main/java/com/viperlevels/listener/DimensionListener.java
- src/main/java/com/viperlevels/listener/EnchantListener.java

---

## PARTE 8: Inventory GUI Framework
**Obiettivo**: Sistema GUI completo per visualizzare requisiti e gestire bypass

### Passaggi:
1. Implementare framework base da documentazione (InventoryHandler, InventoryButton, InventoryGUI, GUIManager, GUIListener)
2. MainMenuGUI - menu principale con navigazione
3. RulesViewGUI - visualizza tutte le regole attive
4. PlayerStatusGUI - mostra livelli player e cosa può usare
5. BypassManageGUI - admin GUI per gestire bypass
6. ItemPreviewGUI - preview oggetti bloccati con requisiti
7. Paginazione per liste lunghe
8. XMaterial per compatibilità

### File creati:
- src/main/java/com/viperlevels/inventory/InventoryHandler.java
- src/main/java/com/viperlevels/inventory/InventoryButton.java
- src/main/java/com/viperlevels/inventory/InventoryGUI.java
- src/main/java/com/viperlevels/inventory/gui/GUIManager.java
- src/main/java/com/viperlevels/inventory/gui/GUIListener.java
- src/main/java/com/viperlevels/inventory/impl/MainMenuGUI.java
- src/main/java/com/viperlevels/inventory/impl/RulesViewGUI.java
- src/main/java/com/viperlevels/inventory/impl/PlayerStatusGUI.java
- src/main/java/com/viperlevels/inventory/impl/BypassManageGUI.java
- src/main/java/com/viperlevels/inventory/impl/ItemPreviewGUI.java
- src/main/java/com/viperlevels/inventory/PaginatedGUI.java

---

## PARTE 9: Sistema Comandi
**Obiettivo**: Comandi admin completi

### Passaggi:
1. Creare CommandManager con registry comandi
2. /viperlevels reload - ricarica config + rules
3. /viperlevels check <player> <target> - verifica requisiti
4. /viperlevels info <item|block> - mostra regole per materiale
5. /viperlevels bypass <player> <category|item> - gestisci bypass
6. /viperlevels gui [player] - apre GUI principale
7. /viperlevels stats - statistiche plugin
8. Tab completion completo
9. Permessi per ogni comando

### File creati:
- src/main/java/com/viperlevels/command/CommandManager.java
- src/main/java/com/viperlevels/command/ViperCommand.java
- src/main/java/com/viperlevels/command/impl/ReloadCommand.java
- src/main/java/com/viperlevels/command/impl/CheckCommand.java
- src/main/java/com/viperlevels/command/impl/InfoCommand.java
- src/main/java/com/viperlevels/command/impl/BypassCommand.java
- src/main/java/com/viperlevels/command/impl/GuiCommand.java
- src/main/java/com/viperlevels/command/impl/StatsCommand.java
- src/main/java/com/viperlevels/command/TabCompleter.java

---

## PARTE 10: Sistema Messaggi e Penalità
**Obiettivo**: Gestione centralizzata messaggi e applicazione penalità

### Passaggi:
1. MessageSender con formatting unificato
2. Supporto placeholder multipli
3. PenaltyApplier che applica penalties da config
4. cancel event
5. damage item (con amount configurabile)
6. Cooldown system per spam prevention
7. Sound/particle feedback (XSound, XParticle)

### File creati:
- src/main/java/com/viperlevels/messaging/MessageSender.java
- src/main/java/com/viperlevels/messaging/MessageFormatter.java
- src/main/java/com/viperlevels/penalty/PenaltyApplier.java
- src/main/java/com/viperlevels/penalty/PenaltyType.java
- src/main/java/com/viperlevels/util/CooldownManager.java

---

## PARTE 11: Sistema Bypass
**Obiettivo**: Bypass temporanei/permanenti per player/gruppi

### Passaggi:
1. BypassManager con cache + database
2. Bypass per categoria (ALL, BLOCKS, ITEMS, etc)
3. Bypass per singolo materiale
4. Bypass temporaneo con expiry
5. Persistenza database
6. Check bypass nei validators
7. GUI per gestione bypass admin

### File creati:
- src/main/java/com/viperlevels/bypass/BypassManager.java
- src/main/java/com/viperlevels/bypass/BypassType.java
- src/main/java/com/viperlevels/bypass/BypassEntry.java
- src/main/java/com/viperlevels/bypass/BypassChecker.java

---

## PARTE 12: Utilities e Finalizzazione
**Obiettivo**: Utility classes, integration hooks, polish finale

### Passaggi:
1. McMMOIntegration - wrapper API mcMMO
2. ItemsAdderIntegration - check soft dependency
3. MaterialResolver - risolve XMaterial + ItemsAdder namespaces
4. DebugLogger per debug mode
5. MetricsCollector per statistiche interne
6. Registrazione tutti listeners in ViperLevels main
7. Shutdown graceful con database close
8. Version checker
9. Default config generation

### File creati:
- src/main/java/com/viperlevels/integration/McMMOIntegration.java
- src/main/java/com/viperlevels/integration/ItemsAdderIntegration.java
- src/main/java/com/viperlevels/util/MaterialResolver.java
- src/main/java/com/viperlevels/util/DebugLogger.java
- src/main/java/com/viperlevels/util/MetricsCollector.java

---

## PARTE 13: Config Completo e Testing
**Obiettivo**: Config.yml completo con esempi reali

### Passaggi:
1. Popolare config.yml con esempi per ogni sezione
2. Material groups per diamond, netherite, end-game
3. Regole esempio per items comuni
4. Regole blocchi (ore, nether blocks)
5. Armature con enchant multi-level
6. Enchanting table tiers
7. Potions e food
8. Mobs e dimensions
9. Messaggi in italiano
10. Testing manuale ogni feature

### File aggiornati:
- src/main/resources/config.yml (completo)

---

## PARTE 14: Ottimizzazioni Finali
**Obiettivo**: Performance tuning e best practices

### Passaggi:
1. Profiling event handlers
2. Ottimizzare query database con indexes
3. Batch loading rules
4. Async dove possibile
5. Cache warm-up on startup
6. Memory leak check
7. Thread-safe collections
8. Javadoc (opzionale, only where critical)

---

## Riepilogo File Totali

### Core (4 file)
- ViperLevels.java
- build.gradle
- manifest.kod
- plugin.yml

### Condition System (5 file)
- McMMOSkill, SkillRequirement, Condition, ConditionParser, ConditionEvaluator

### Config (6 file)
- config.yml, ConfigManager, SettingsConfig, MessagesConfig, PenaltiesConfig, PlaceholderReplacer

### Database (7 file)
- DatabaseManager, DatabaseType, SQLiteDatabase, MySQLDatabase, BypassRepository, CacheRepository, StatsRepository

### Cache (3 file)
- CacheManager, CachedSkillData, CacheService

### Rules (7 file)
- RuleType, ActionType, Rule, MaterialGroup, RuleManager, ValidationResult, RuleValidator

### Listeners (8 file)
- BlockListener, ItemListener, CraftListener, ArmorListener, CombatListener, ConsumeListener, DimensionListener, EnchantListener

### GUI (11 file)
- InventoryHandler, InventoryButton, InventoryGUI, GUIManager, GUIListener, MainMenuGUI, RulesViewGUI, PlayerStatusGUI, BypassManageGUI, ItemPreviewGUI, PaginatedGUI

### Commands (8 file)
- CommandManager, ViperCommand, ReloadCommand, CheckCommand, InfoCommand, BypassCommand, GuiCommand, StatsCommand, TabCompleter

### Messaging & Penalties (5 file)
- MessageSender, MessageFormatter, PenaltyApplier, PenaltyType, CooldownManager

### Bypass (4 file)
- BypassManager, BypassType, BypassEntry, BypassChecker

### Utilities (5 file)
- McMMOIntegration, ItemsAdderIntegration, MaterialResolver, DebugLogger, MetricsCollector

**TOTALE: circa 73 file Java + 3 configurazione**

---

## Note Tecniche

### Dipendenze Build
- Spigot API 1.21.1
- mcMMO Classic (compileOnly)
- ItemsAdder (compileOnly)
- HikariCP (implementation, in manifest.kod)
- XSeries (embedded)
- Lombok (compileOnly)

### Pattern Utilizzati
- Singleton per Manager
- Factory per Database
- Builder per GUI buttons
- Repository per data access
- Strategy per validation
- Observer per eventi

### Threading
- Main thread: event handling, GUI
- Async: database queries, cache warmup
- Scheduled: cache cleanup, stats collection

---

## Registrazione Progressi

### Sessione 1 (Setup Base)
- [ ] Parte 1 completata
- [ ] Build funzionante
- [ ] Plugin caricato

### Sessione 2 (Core Logic)
- [ ] Parte 2 completata
- [ ] Parser condizioni testato
- [ ] Evaluator funzionante

### Sessione 3 (Config + DB)
- [ ] Parte 3 completata
- [ ] Parte 4 completata
- [ ] Database inizializzato

### Sessione 4 (Cache + Rules)
- [ ] Parte 5 completata
- [ ] Parte 6 completata
- [ ] Rules caricati da config

### Sessione 5 (Events)
- [ ] Parte 7 completata
- [ ] Tutti listener attivi
- [ ] Restrizioni funzionanti

### Sessione 6 (GUI)
- [ ] Parte 8 completata
- [ ] Tutte GUI implementate
- [ ] Navigazione fluida

### Sessione 7 (Commands + Finalizzazione)
- [ ] Parte 9 completata
- [ ] Parte 10 completata
- [ ] Parte 11 completata
- [ ] Parte 12 completata

### Sessione 8 (Config & Testing)
- [ ] Parte 13 completata
- [ ] Parte 14 completata
- [ ] Plugin production-ready

---

## Priorità Features

### Must Have (Core)
1. Parsing condizioni AND/OR
2. Validation livelli mcMMO
3. Block/Item restrictions
4. Config reload
5. Database persistence

### Should Have (Important)
1. GUI completa
2. Cache system
3. Bypass system
4. Tutti gli event listeners
5. Messaggi personalizzabili

### Nice to Have (Polish)
1. Stats collection
2. Debug mode avanzato
3. ItemsAdder integration
4. Enchanting table tiers
5. Dimension restrictions

---

Fine del piano di sviluppo.
Ogni parte sarà registrata con checkbox al completamento.