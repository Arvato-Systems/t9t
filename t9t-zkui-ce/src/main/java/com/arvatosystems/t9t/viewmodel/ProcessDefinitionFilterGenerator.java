package com.arvatosystems.t9t.viewmodel;

import com.arvatosystems.t9t.bpmn2.ProcessDefinitionDTO;
import com.arvatosystems.t9t.component.ext.IFilterGenerator;
import de.jpaw.bonaparte.api.SearchFilters;
import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.AsciiFilter;
import de.jpaw.bonaparte.pojos.api.LongFilter;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Named("processDefinition")
public class ProcessDefinitionFilterGenerator implements IFilterGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessDefinitionFilterGenerator.class);

    @Override
    public SearchFilter createFilter(BonaPortable data) {
        AsciiFilter f = new AsciiFilter();
        f.setFieldName("processDefinitionId");
        f.setEqualsValue(((ProcessDefinitionDTO)data).getProcessDefinitionId());
        return f;
    }
}
