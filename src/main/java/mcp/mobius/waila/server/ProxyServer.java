package mcp.mobius.waila.server;

import java.lang.reflect.Method;

import mcp.mobius.waila.Waila;
import mcp.mobius.waila.addons.vanillamc.HUDHandlerFurnace;
import mcp.mobius.waila.addons.vanillamc.HUDHandlerVanilla;
import mcp.mobius.waila.api.IWailaRegistrar;
import mcp.mobius.waila.api.impl.ModuleRegistrar;
import mcp.mobius.waila.handlers.DecoratorFMP;
import mcp.mobius.waila.handlers.HUDHandlerFMP;
import moddedmite.waila.handlers.HUDHandlerMITE;

public class ProxyServer {

    public ProxyServer() {}

    public void registerHandlers() {}

    public void registerMods() {
        HUDHandlerVanilla.register();
        HUDHandlerFurnace.register();

        HUDHandlerFMP.register();
        DecoratorFMP.register();

        HUDHandlerMITE.register();
    }

    public void registerIMCs() {
        for (String s : ModuleRegistrar.instance().IMCRequests.keySet()) {
            this.callbackRegistration(s, ModuleRegistrar.instance().IMCRequests.get(s));
        }
    }

    public void callbackRegistration(String method, String modname) {
        String[] splitName = method.split("\\.");
        String methodName = splitName[splitName.length - 1];
        String className = method.substring(0, method.length() - methodName.length() - 1);

        Waila.log.info(String.format("Trying to reflect %s %s", className, methodName));

        try {
            Class<?> reflectClass = Class.forName(className);
            Method reflectMethod = reflectClass.getDeclaredMethod(methodName, IWailaRegistrar.class);
            reflectMethod.invoke(null, ModuleRegistrar.instance());

            Waila.log.info(String.format("Success in registering %s", modname));

        } catch (ClassNotFoundException e) {
            Waila.log.warn(String.format("Could not find class %s", className));
        } catch (NoSuchMethodException e) {
            Waila.log.warn(String.format("Could not find method %s", methodName));
        } catch (Exception e) {
            Waila.log.warn(String.format("Exception while trying to access the method : %s", e));
        }
    }

    public Object getFont() {
        return null;
    }
}
