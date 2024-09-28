//package mcp.mobius.waila.handlers.nei;
//
//import java.util.regex.Pattern;
//
//import net.minecraft.ItemStack;
//
//import codechicken.nei.SearchField;
//import codechicken.nei.SearchField.ISearchProvider;
//import codechicken.nei.api.ItemFilter;
//import mcp.mobius.waila.utils.ModIdentification;
//
//public class ModNameFilter implements ISearchProvider {
//
//    @Override
//    public ItemFilter getFilter(String searchText) {
//        Pattern pattern = SearchField.getPattern(searchText);
//        return pattern == null ? null : new Filter(pattern);
//    }
//
//    @Override
//    public boolean isPrimary() {
//        return false;
//    }
//
//    public static class Filter implements ItemFilter {
//
//        Pattern pattern;
//
//        public Filter(Pattern pattern) {
//            this.pattern = pattern;
//        }
//
//        @Override
//        public boolean matches(ItemStack itemstack) {
//            return this.pattern.matcher(ModIdentification.nameFromStack(itemstack).toLowerCase()).find();
//        }
//
//    }
//
//}
