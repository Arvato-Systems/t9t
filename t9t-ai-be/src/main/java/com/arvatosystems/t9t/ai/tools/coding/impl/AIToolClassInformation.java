package com.arvatosystems.t9t.ai.tools.coding.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.reflections.Reflections;

import com.arvatosystems.t9t.ai.service.IAITool;
import com.arvatosystems.t9t.ai.tools.coding.AIToolExplainClass;
import com.arvatosystems.t9t.ai.tools.coding.AIToolExplainClassResult;
import com.arvatosystems.t9t.ai.tools.coding.AIToolFieldDescription;
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

@Named(AIToolExplainClass.my$PQON)
@Singleton
@Startup(54627)
public class AIToolClassInformation implements IAITool<AIToolExplainClass, AIToolExplainClassResult>, StartupOnly {
    // private static final Logger LOGGER = LoggerFactory.getLogger(AIToolClassInformation.class);

    private static final Map<String, Class<? extends BonaPortable>> ALL_BONAPORTABLES = new ConcurrentHashMap<>(10000);

    @Override
    public void onStartup() {
        final Reflections t9t = ReflectionsPackageCache.get(MessagingUtil.TWENTYEIGHT_PACKAGE_PREFIX);
        for (final Class<? extends BonaPortable> clazz : t9t.getSubTypesOf(BonaPortable.class)) {
            ALL_BONAPORTABLES.put(clazz.getSimpleName().toLowerCase(), clazz);
        }
    }


    @Override
    public AIToolExplainClassResult performToolCall(final RequestContext ctx, final AIToolExplainClass request) {
        final AIToolExplainClassResult result = new AIToolExplainClassResult();
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
        final List<AIToolFieldDescription> fields = new ArrayList<>(cd.getFields().size());
        result.setFields(fields);
        for (final FieldDefinition field: cd.getFields()) {
            final AIToolFieldDescription fd = new AIToolFieldDescription();
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
