package com.arvatosystems.t9t.ai.tools.coding.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.reflections.Reflections;

import com.arvatosystems.t9t.ai.service.IAiTool;
import com.arvatosystems.t9t.ai.tools.coding.AiToolExplainClass;
import com.arvatosystems.t9t.ai.tools.coding.AiToolExplainClassResult;
import com.arvatosystems.t9t.ai.tools.coding.AiToolFieldDescription;
import com.arvatosystems.t9t.base.MessagingUtil;
import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.BonaPortableClass;
import de.jpaw.bonaparte.core.BonaPortableFactory;
import de.jpaw.bonaparte.pojos.meta.AlphanumericElementaryDataItem;
import de.jpaw.bonaparte.pojos.meta.BasicNumericElementaryDataItem;
import de.jpaw.bonaparte.pojos.meta.ClassDefinition;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import de.jpaw.dp.Startup;
import de.jpaw.dp.StartupOnly;
import de.jpaw.xenums.init.ReflectionsPackageCache;

@Named(AiToolExplainClass.my$PQON)
@Singleton
@Startup(54627)
public class AiToolClassInformation implements IAiTool<AiToolExplainClass, AiToolExplainClassResult>, StartupOnly {
    // private static final Logger LOGGER = LoggerFactory.getLogger(AiToolClassInformation.class);

    private static final Map<String, Class<? extends BonaPortable>> ALL_BONAPORTABLES = new ConcurrentHashMap<>(10000);

    @Override
    public void onStartup() {
        final Reflections t9t = ReflectionsPackageCache.get(MessagingUtil.TWENTYEIGHT_PACKAGE_PREFIX);
        for (final Class<? extends BonaPortable> clazz : t9t.getSubTypesOf(BonaPortable.class)) {
            ALL_BONAPORTABLES.put(clazz.getSimpleName().toLowerCase(), clazz);
        }
    }


    @Override
    public AiToolExplainClassResult performToolCall(final RequestContext ctx, final AiToolExplainClass request) {
        final AiToolExplainClassResult result = new AiToolExplainClassResult();
        final String nameInLower = request.getClassName().toLowerCase();
        final Class<? extends BonaPortable> clazz = ALL_BONAPORTABLES.get(nameInLower);
        if (clazz == null) {
            result.setDescription("There is no class of name " + request.getClassName());
            return result;
        }
        final BonaPortableClass<? extends BonaPortable> bclass = BonaPortableFactory.getBClassForFqon(clazz.getCanonicalName());
        final ClassDefinition cd = bclass.getMetaData();
        result.setClassName(clazz.getSimpleName());
        result.setPackageName(T9tUtil.getPackageName(clazz.getCanonicalName()));
        if (bclass.getParent() != null) {
            result.setParentClass(T9tUtil.getSimpleName(bclass.getParent().getPqon()));
        }
        if (bclass.getReturns() != null) {
            result.setResponseClass(T9tUtil.getSimpleName(bclass.getReturns().getPqon()));
        }
        result.setDescription(cd.getJavaDoc());
        final List<AiToolFieldDescription> fields = new ArrayList<>(cd.getFields().size());
        result.setFields(fields);
        for (final FieldDefinition field: cd.getFields()) {
            final AiToolFieldDescription fd = new AiToolFieldDescription();
            fd.setName(field.getName());
            fd.setType(field.getDataType());
            fd.setDescription(T9tUtil.nvl(field.getJavaDoc(), field.getRegularComment(), field.getTrailingComment()));
            if (field instanceof AlphanumericElementaryDataItem alnumFd) {
                fd.setLength(alnumFd.getLength());
            }
            if (field instanceof BasicNumericElementaryDataItem numFd) {
                fd.setTotalDigits(numFd.getTotalDigits());
                fd.setFractionalDigits(numFd.getDecimalDigits());
                fd.setIsSigned(numFd.getIsSigned());
            }
            fields.add(fd);
        }
        return result;
    }
}
