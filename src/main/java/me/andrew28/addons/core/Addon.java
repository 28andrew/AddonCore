package me.andrew28.addons.core;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.Converters;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Getter;
import me.andrew28.addons.core.annotations.*;
import org.bstats.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Andrew Tran
 */
public abstract class Addon extends JavaPlugin {

    private int loadedExpressions = 0, loadedTypes = 0, loadedEffects = 0, loadedConditions = 0, loadedEvents = 0;

    private List<ASAElement> asaElements = new ArrayList<>();
    private List<AddonCommand> addonCommands = new ArrayList<>();

    private Metrics metrics;

    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().getPlugin("Skript") == null) {
            getLogger().warning("Please install the plugin Skript (https://github.com/bensku/Skript/releases)");
            getServer().getPluginManager().disablePlugin(this);
        }
        metrics = new Metrics(this);
        onAddonEnable();
    }

    public abstract void onAddonEnable();

    public void register(String pckg) {
        getLogger().info("Loading Skript Elements");
        Skript.registerAddon(this);
        registerClasses(pckg);
        getLogger().info(String.format("%d Expressions | %d Types | %d Effects | %d Conditions | %d Events"
                , getLoadedExpressions(), getLoadedTypes(), getLoadedEffects(), getLoadedConditions(), getLoadedEvents()));
    }

    public Metrics getMetrics() {
        return metrics;
    }

    public List<AddonCommand> getAddonCommands() {
        return addonCommands;
    }

    public void registerClass(Class<?> cls) {
        try {
            if (cls.isAnnotationPresent(DoNotRegister.class)) {
                return;
            }
            ElementSyntax[] elementSyntaxes = SyntaxElementUtil.getSyntax(cls);
            List<String> syntaxesList = new ArrayList<>();
            for (ElementSyntax elementSyntax : elementSyntaxes) {
                syntaxesList.addAll(Arrays.asList(elementSyntax.getRawSyntaxes()));
            }
            String[] syntaxes = syntaxesList.stream().map(s -> "[" + getName().toLowerCase() + "] " + s).toArray(String[]::new);
            ExpressionType expressionType = null;
            if (cls.isAnnotationPresent(ExprType.class)) {
                expressionType = cls.getAnnotation(ExprType.class).value();
            }

            String name = cls.getCanonicalName();
            if (cls.isAnnotationPresent(Name.class)) {
                name = cls.getDeclaredAnnotation(Name.class).value();
            }

            String description = "";
            if (cls.isAnnotationPresent(Description.class)) {
                description = cls.getDeclaredAnnotation(Description.class).value();
            }

            String[][] examples = new String[0][0];
            if (cls.isAnnotationPresent(Examples.class) || cls.isAnnotationPresent(Example.class)) {
                List<List<String>> examplesList = new ArrayList<>();
                for (Example example : cls.isAnnotationPresent(Examples.class)
                        ? cls.getDeclaredAnnotation(Examples.class).value()
                        : new Example[]{cls.getDeclaredAnnotation(Example.class)}) {
                    List<String> exampleLines = Arrays.asList(example.value());
                    examplesList.add(exampleLines);
                }
                examples = examplesList.stream()
                        .map(List::toArray)
                        .toArray(String[][]::new);
            }

            Boolean document = !cls.isAnnotationPresent(DoNotDocument.class);

            if (ASAPropertyExpression.class.isAssignableFrom(cls)) {
                if (expressionType == null) {
                    expressionType = ExpressionType.PROPERTY;
                }
                ASAPropertyExpression asaPropertyExpression = (ASAPropertyExpression) cls.newInstance();
                if (syntaxes.length < 2) {
                    throw new RuntimeException(String.format("Property Expression %s does not have two syntaxes which denote the property and the fromType (see docs)", getClass().getCanonicalName()));
                }
                String property = syntaxes[0];
                String fromType = syntaxes[1];
                String[] propertySyntaxes = {
                        "[" + getName().toLowerCase() + "] " + "[the] " + property + " of %" + fromType + "%", "[" + getName().toLowerCase() + "] " + "%" + fromType + "%'[s] " + property
                };
                Skript.registerExpression(asaPropertyExpression.getClass(), asaPropertyExpression.getReturnType(), expressionType, propertySyntaxes);
                asaElements.add(new ASAElement(name, description, ASAElement.ElementType.PROPERTY_EXPRESSION, elementSyntaxes, asaPropertyExpression.getClass(), document, examples));
                loadedExpressions++;
            } else if (ASAExpression.class.isAssignableFrom(cls)) {
                if (expressionType == null) {
                    expressionType = ExpressionType.SIMPLE;
                }
                ASAExpression asaExpression = (ASAExpression) cls.newInstance();
                Skript.registerExpression(asaExpression.getClass(), asaExpression.getReturnType(), expressionType, syntaxes);
                asaElements.add(new ASAElement(name, description, ASAElement.ElementType.EXPRESSION, elementSyntaxes, asaExpression.getClass(), document, examples));
                loadedExpressions++;
            } else if (ASAType.class.isAssignableFrom(cls)) {
                ASAType asaType = (ASAType) cls.newInstance();
                Classes.registerClass(asaType.getClassInfo());
                asaElements.add(new ASAElement(name, description, ASAElement.ElementType.TYPE, elementSyntaxes, asaType.getClass(), document, examples));
                loadedTypes++;
            } else if (ASAEffect.class.isAssignableFrom(cls)) {
                ASAEffect asaEffect = (ASAEffect) cls.newInstance();
                Skript.registerEffect(asaEffect.getClass(), syntaxes);
                asaElements.add(new ASAElement(name, description, ASAElement.ElementType.EFFECT, elementSyntaxes, asaEffect.getClass(), document, examples));
                loadedEffects++;
            } else if (ASACondition.class.isAssignableFrom(cls)) {
                ASACondition asaCondition = (ASACondition) cls.newInstance();
                Skript.registerCondition(asaCondition.getClass(), syntaxes);
                asaElements.add(new ASAElement(name, description, ASAElement.ElementType.CONDITION, elementSyntaxes, asaCondition.getClass(), document, examples));
                loadedConditions++;
            } else if (ASAEvent.class.isAssignableFrom(cls)) {
                ASAEvent asaEvent = (ASAEvent) cls.newInstance();
                Skript.registerEvent(asaEvent.getName(), asaEvent.getSkriptEvent(), asaEvent.getEventClass(), syntaxes);
                if (asaEvent.getEventValues() != null) {
                    EventValue<?, ?>[] eventValues = asaEvent.getEventValues();
                    for (EventValue<?, ?> eventValue : eventValues) {
                        EventValues.registerEventValue(asaEvent.getEventClass(), eventValue.getValueClass(), (Getter) eventValue, eventValue.getEventTime().getTime());
                    }
                }
                asaElements.add(new ASAElement(name, description, ASAElement.ElementType.EVENT, elementSyntaxes, asaEvent.getClass(), document, examples));
                loadedEvents++;
            } else if (ASAConverter.class.isAssignableFrom(cls)) {
                ASAConverter asaConverter = (ASAConverter) cls.newInstance();
                Converters.registerConverter(asaConverter.getFromClass(), asaConverter.getToClass(), asaConverter);
            }
        } catch (InstantiationException | IllegalAccessException e) {
            getLogger().warning("Current Class: " + cls.getCanonicalName());
            e.printStackTrace();
        }
    }

    public void registerClasses(String pckg) {
        List<Class<?>> classes = new ArrayList<>(ClassFinder.getClasses(getFile(), pckg));
        classes.sort(new Comparator<Class<?>>() {
            @Override
            public int compare(Class<?> o1, Class<?> o2) {
                return getWeight(o1).compareTo(getWeight(o2));
            }

            public Integer getWeight(Class<?> cls) {
                if (ASAType.class.isAssignableFrom(cls)) {
                    return 1;
                } else {
                    return 2;
                }
            }
        });

        for (Class<?> cls : classes) {
            registerClass(cls);
        }
    }

    public void generateDocumentation() {
        File javaPluginFolder = getDataFolder();
        if (!javaPluginFolder.exists()) {
            javaPluginFolder.mkdir();
        }

        File docsFile = new File(javaPluginFolder, "docs.json");

        JSONObject jsonObject = new JSONObject();

        JSONArray eventsArray = new JSONArray();
        JSONArray conditionsArray = new JSONArray();
        JSONArray effectsArray = new JSONArray();
        JSONArray expressionsArray = new JSONArray();
        JSONArray typesArray = new JSONArray();
        for (ASAElement asaElement : asaElements) {
            String name = asaElement.getName();
            String description = asaElement.getDescription();
            String[][] examples = asaElement.getExamples();
            Class<? extends AutoRegisteringSkriptElement> elementClass = asaElement.getElementClass();
            ASAElement.ElementType elementType = asaElement.getElementType();

            ElementSyntax[] elementSyntaxes = asaElement.getElementSyntaxes();

            JSONObject elementJSONObject = new JSONObject();
            elementJSONObject.put("name", name);
            elementJSONObject.put("description", description);
            elementJSONObject.put("class", elementClass.getCanonicalName());

            JSONArray examplesJSONArray = new JSONArray();
            for (String[] exampleLines : examples) {
                JSONArray exampleLinesJSONArray = new JSONArray();
                exampleLinesJSONArray.addAll(Arrays.asList(exampleLines));
                examplesJSONArray.add(exampleLinesJSONArray);
            }
            elementJSONObject.put("examples", examplesJSONArray);

            /* Syntax */
            JSONArray syntaxesJSONArray = new JSONArray();
            switch (elementType) {
                case CONDITION:
                case EFFECT:
                case EVENT:
                case EXPRESSION:
                    for (ElementSyntax elementSyntax : elementSyntaxes) {
                        for (String rawSyntax : elementSyntax.getRawSyntaxes()) {
                            JSONObject syntaxJSONObject = new JSONObject();
                            syntaxJSONObject.put("syntax", rawSyntax);
                            if (elementSyntax.isUsingBinds()) {
                                JSONArray bindsArray = new JSONArray();
                                bindsArray.addAll(Arrays.asList(elementSyntax.getBinds()));
                                syntaxJSONObject.put("binds", bindsArray);
                            }
                            syntaxesJSONArray.add(syntaxJSONObject);
                        }
                    }
                    elementJSONObject.put("syntaxes", syntaxesJSONArray);
                    break;
                case PROPERTY_EXPRESSION:
                    for (ElementSyntax elementSyntax : elementSyntaxes) {
                        String[] rawSyntaxes = elementSyntax.getRawSyntaxes();
                        String property = rawSyntaxes[0];
                        String fromType = rawSyntaxes[1];
                        String[] syntaxes = {"[the] " + property + " of %" + fromType + "%", "%" + fromType + "%'[s] " + property};
                        for (String syntax : syntaxes) {
                            JSONObject syntaxJSONObject = new JSONObject();
                            syntaxJSONObject.put("syntax", syntax);
                            if (elementSyntax.isUsingBinds()) {
                                JSONArray bindsArray = new JSONArray();
                                bindsArray.addAll(Arrays.asList(elementSyntax.getBinds()));
                                syntaxJSONObject.put("binds", bindsArray);
                            }
                            syntaxesJSONArray.add(syntaxJSONObject);
                        }
                    }
                    break;
                case TYPE:
                    for (ElementSyntax elementSyntax : elementSyntaxes) {
                        JSONObject syntaxJSONObject = new JSONObject();
                        syntaxJSONObject.put("syntax", elementSyntax.getRawSyntaxes());
                        syntaxesJSONArray.add(syntaxJSONObject);
                    }
                    break;
            }

            AutoRegisteringSkriptElement autoRegisteringSkriptElement = null;
            try {
                autoRegisteringSkriptElement = elementClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
            /* Element Specific Handling */
            switch (elementType) {
                case EVENT:
                    ASAEvent asaEvent = (ASAEvent) autoRegisteringSkriptElement;
                    elementJSONObject.put("eventClass", asaEvent.getEventClass().getCanonicalName());
                    elementJSONObject.put("cancellable", Cancellable.class.isAssignableFrom(asaEvent.getEventClass()));
                    EventValue[] eventValues = asaEvent.getEventValues();
                    JSONArray eventValuesJSONArray = new JSONArray();
                    List<String> alreadyRegisteredClasses = new ArrayList<>();
                    for (EventValue eventValue : eventValues) {
                        JSONObject eventValueJSONObject = new JSONObject();
                        String canonicalName = eventValue.getValueClass().getCanonicalName();
                        if (alreadyRegisteredClasses.contains(canonicalName)) {
                            continue;
                        }
                        alreadyRegisteredClasses.add(canonicalName);
                        eventValueJSONObject.put("typeClass", canonicalName);
                        ClassInfo<?> classInfo = Classes.getExactClassInfo(eventValue.getValueClass());
                        if (classInfo != null) {
                            eventValueJSONObject.put("type", classInfo.getCodeName());
                        }
                        eventValuesJSONArray.add(eventValueJSONObject);
                    }
                    elementJSONObject.put("eventValues", eventValuesJSONArray);
                    break;
                case TYPE:
                    ASAType asaType = (ASAType) autoRegisteringSkriptElement;
                    ClassInfo<?> classInfo = asaType.getClassInfo();
                    elementJSONObject.put("typeClass", classInfo.getC().getCanonicalName());
                    elementJSONObject.put("codename", classInfo.getCodeName());
                    JSONArray patternsArray = new JSONArray();
                    patternsArray.addAll(Arrays.asList(Arrays.stream(classInfo.getUserInputPatterns()).map(Pattern::pattern).toArray(String[]::new)));
                    elementJSONObject.put("patterns", patternsArray);
                    elementJSONObject.put("type", classInfo.getCodeName());
                    break;
                case EXPRESSION:
                    ASAExpression asaExpression = (ASAExpression) autoRegisteringSkriptElement;
                    elementJSONObject.put("returnTypeClass", asaExpression.getReturnType().getCanonicalName());
                    ClassInfo returnTypeClassInfo = Classes.getExactClassInfo(asaExpression.getReturnType());
                    if (returnTypeClassInfo != null) {
                        elementJSONObject.put("returnType", returnTypeClassInfo.getCodeName());
                    }
                    break;
            }
            /* Add to Element Specific array */
            switch (elementType) {
                case CONDITION:
                    conditionsArray.add(elementJSONObject);
                    break;
                case EFFECT:
                    effectsArray.add(elementJSONObject);
                    break;
                case EVENT:
                    eventsArray.add(elementJSONObject);
                    break;
                case PROPERTY_EXPRESSION:
                    expressionsArray.add(elementJSONObject);
                    break;
                case EXPRESSION:
                    expressionsArray.add(elementJSONObject);
                    break;
                case TYPE:
                    typesArray.add(elementJSONObject);
                    break;
            }
        }

        jsonObject.put("events", eventsArray);
        jsonObject.put("conditions", conditionsArray);
        jsonObject.put("effects", effectsArray);
        jsonObject.put("expressions", expressionsArray);
        jsonObject.put("types", typesArray);

        try (Writer out = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(docsFile), "UTF-8"));) {
            out.write(jsonObject.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public CommandExecutor getCommand(AddonCommand[] addonCommands) {
        List<AddonCommand> addonCommandsList = new ArrayList<>(Arrays.asList(addonCommands));
        addonCommandsList.add(new AddonCommand() {
            @Override
            public String[] getSubCommands() {
                return new String[]{"documentation", "docs", "generatedocs"};
            }

            @Override
            public String usage() {
                return "(documentation|docs|generatedocs)";
            }

            @Override
            public String description() {
                return "Generate documentation files";
            }

            @Override
            public void handle(CommandSender commandSender, String alias, List<String> arguments) {
                if (commandSender.hasPermission(getName() + ".command.generatedocs") || commandSender.isOp()) {
                    commandSender.sendMessage(ChatColor.GREEN + "Generating documentation..");
                    generateDocumentation();
                    commandSender.sendMessage(ChatColor.GREEN + "Done generating documentation!");
                }
            }
        });
        addonCommandsList.add(new AddonCommand() {
            @Override
            public String[] getSubCommands() {
                return new String[]{"help", "?"};
            }

            @Override
            public String usage() {
                return "(help|?)";
            }

            @Override
            public String description() {
                return "Retrieve the help";
            }

            @Override
            public void handle(CommandSender commandSender, String alias, List<String> arguments) {
                commandSender.sendMessage("     ");
                if (commandSender.hasPermission(getName() + ".command.help") || commandSender.isOp()) {
                    commandSender.sendMessage(ChatColor.GREEN + getName() + " Help");
                    for (AddonCommand addonCommand : getAddonCommands()) {
                        commandSender.sendMessage(ChatColor.GREEN + "/" + alias + " " + addonCommand.usage() + " ");
                        commandSender.sendMessage(ChatColor.YELLOW + addonCommand.description());
                    }
                }
            }
        });
        this.addonCommands.addAll(addonCommandsList);
        return (commandSender, command, s, arguments) -> {
            if (arguments.length > 0) {
                //Has arguments
                String subcommand = arguments[0];
                List<String> argumentsList = new ArrayList<>(Arrays.asList(arguments));
                argumentsList.remove(0); //Remove subcommand
                for (AddonCommand addonCommand : addonCommandsList) {
                    for (String addonSubCommand : addonCommand.getSubCommands()) {
                        if (addonSubCommand.equals(subcommand)) {
                            addonCommand.handle(commandSender, s, argumentsList);
                            return true;
                        }
                    }
                }
            } else {
                commandSender.sendMessage(ChatColor.YELLOW + getName() + ChatColor.GREEN + " - An addon by " + ChatColor.YELLOW + String.join(", ", getDescription().getAuthors()));
                commandSender.sendMessage(ChatColor.GREEN + "Using Addon Core from Andrew's Skript Addons");
                commandSender.sendMessage(ChatColor.GREEN + "Version: " + ChatColor.YELLOW + getDescription().getVersion());
            }
            return true;
        };
    }

    public int getLoadedExpressions() {
        return loadedExpressions;
    }

    public int getLoadedTypes() {
        return loadedTypes;
    }

    public int getLoadedEffects() {
        return loadedEffects;
    }

    public int getLoadedConditions() {
        return loadedConditions;
    }

    public int getLoadedEvents() {
        return loadedEvents;
    }

    public void assertNotNull(Object object, String message) throws NullExpressionException {
        if (object == null) {
            throw new NullExpressionException(message);
        }
    }

    public static interface AddonCommand {
        String[] getSubCommands();

        String usage();

        String description();

        void handle(CommandSender commandSender, String alias, List<String> arguments);
    }
}
