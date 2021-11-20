/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.arvatosystems.t9t.solr.be.impl

import com.arvatosystems.t9t.base.search.EnumFilter
import com.arvatosystems.t9t.base.search.XenumFilter
import com.arvatosystems.t9t.base.services.IEnumResolver
import com.arvatosystems.t9t.solr.be.IFilterToSolrConverter
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.pojos.api.AndFilter
import de.jpaw.bonaparte.pojos.api.AsciiFilter
import de.jpaw.bonaparte.pojos.api.BooleanFilter
import de.jpaw.bonaparte.pojos.api.DayFilter
import de.jpaw.bonaparte.pojos.api.DecimalFilter
import de.jpaw.bonaparte.pojos.api.DoubleFilter
import de.jpaw.bonaparte.pojos.api.FieldFilter
import de.jpaw.bonaparte.pojos.api.FloatFilter
import de.jpaw.bonaparte.pojos.api.InstantFilter
import de.jpaw.bonaparte.pojos.api.IntFilter
import de.jpaw.bonaparte.pojos.api.LongFilter
import de.jpaw.bonaparte.pojos.api.NotFilter
import de.jpaw.bonaparte.pojos.api.NullFilter
import de.jpaw.bonaparte.pojos.api.OrFilter
import de.jpaw.bonaparte.pojos.api.SearchFilter
import de.jpaw.bonaparte.pojos.api.TimeFilter
import de.jpaw.bonaparte.pojos.api.TimestampFilter
import de.jpaw.bonaparte.pojos.api.UnicodeFilter
import de.jpaw.bonaparte.pojos.api.UuidFilter
import de.jpaw.dp.Inject
import de.jpaw.dp.Singleton
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@AddLogger
@Singleton
class FilterToSolrConverter implements IFilterToSolrConverter {

    static final DateTimeFormatter isoFmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    static final DateTimeFormatter isoDay = DateTimeFormatter.ISO_LOCAL_DATE

    @Inject IEnumResolver enumResolver

    def private static forSolr(String s) {
        if (s !== null) {
            s.replace(":", "\\:").replace(" ", "\\ ").replace("-", "\\-").replace("%", "*")
        }
    }

    def private static forSolr(LocalDate t) {
        if (t !== null)
            isoDay.format(t) + "T00\\:00\\:00Z"
        else
            "*"
    }

    def private static forSolr(LocalTime t) {
        if (t !== null)
            isoFmt.format(t).replace(":", "\\:")
        else
            "*"
    }

    def private static forSolr(LocalDateTime t) {
        if (t !== null)
            isoFmt.format(t).replace(":", "\\:") + "Z"
        else
            "*"
    }

    def private static forSolr(Instant t) {
        if (t !== null)
            isoFmt.format(t).replace(":", "\\:") + "Z"
        else
            "*"
    }

    def dispatch toSolr(UuidFilter it)      '''«equalsValue.toString.replace("-", "\\-")»'''

    def dispatch toSolr(UnicodeFilter it)   '''«equalsValue.forSolr ?: likeValue.forSolr»'''

    def dispatch toSolr(AsciiFilter it)     '''«equalsValue.forSolr ?: likeValue.forSolr»'''

    def dispatch toSolr(IntFilter it) {
        if (equalsValue !== null) {
            return equalsValue
        }
        return '''[«lowerBound ?: '*'» TO «upperBound ?: '*'»]'''

    }

    def dispatch toSolr(LongFilter it) {
        if (equalsValue !== null) {
            return equalsValue
        }
        return '''[«lowerBound ?: '*'» TO «upperBound ?: '*'»]'''
    }

    def dispatch toSolr(DecimalFilter it) {
        if (equalsValue !== null) {
            return equalsValue
        }
        return '''[«lowerBound ?: '*'» TO «upperBound ?: '*'»]'''
    }

    def dispatch toSolr(TimeFilter it) {
        if (equalsValue !== null) {
            return equalsValue.forSolr
        }
        return '''[«lowerBound.forSolr ?: '*'» TO «upperBound.forSolr ?: '*'»]'''
    }

    def dispatch toSolr(InstantFilter it) {
        if (equalsValue !== null) {
            return equalsValue.forSolr
        }
        return '''[«lowerBound.forSolr ?: '*'» TO «upperBound.forSolr ?: '*'»]'''
    }

    def dispatch toSolr(TimestampFilter it) {
        if (equalsValue !== null) {
            return equalsValue.forSolr
        }
        return '''[«lowerBound.forSolr ?: '*'» TO «upperBound.forSolr ?: '*'»]'''
    }

    def dispatch toSolr(DayFilter it) {
        if (equalsValue !== null) {
            return equalsValue.forSolr
        }
        return '''[«lowerBound.forSolr ?: '*'» TO «upperBound.forSolr ?: '*'»]'''
    }

    def dispatch toSolr(DoubleFilter it) {
        if (equalsValue !== null) {
            return equalsValue
        }
        return '''[«lowerBound ?: '*'» TO «upperBound ?: '*'»]'''
    }

    def dispatch toSolr(FloatFilter it) {
        if (equalsValue !== null) {
            return equalsValue
        }
        return '''[«lowerBound ?: '*'» TO «upperBound ?: '*'»]'''
    }

    //def dispatch toSolr(NullFilter it)      '''[* TO *]'''

    def dispatch toSolr(BooleanFilter it)   { Boolean.toString(booleanValue) } // '''«if (booleanValue) { 'true' } else { 'false' }»'''

    def dispatch toSolr(EnumFilter filter) {
        val what = filter.equalsToken ?: enumResolver.getTokenByPqonAndInstance(filter.enumPqon, filter.equalsName)
        if (what === null)
            return "null"
        else
            return what.toString.forSolr
    }

    def dispatch toSolr(XenumFilter filter) {
        val what = filter.equalsToken ?: enumResolver.getTokenByXEnumPqonAndInstance(filter.xenumPqon, filter.equalsName)
        if (what === null)
            return "null"
        else
            return what.toString.forSolr
    }

    override String toSolrCondition(SearchFilter sc) {
        if (sc !== null)
            return sc.toSolrConditionInternal.toString
        return null
    }

    def dispatch toSolrConditionInternal(NotFilter it) {
        if (filter instanceof NullFilter) {
            // special case: use specific syntax
            val f = filter as NullFilter
            '''+«f.fieldName»:[* TO *]'''
        } else {
            '''NOT («filter.toSolrConditionInternal»)'''
        }
    }

    def dispatch toSolrConditionInternal(AndFilter it)       '''(«filter1.toSolrConditionInternal») AND («filter2.toSolrConditionInternal»)'''

    def dispatch toSolrConditionInternal(OrFilter it)        '''(«filter1.toSolrConditionInternal») OR («filter2.toSolrConditionInternal»)'''

    def dispatch toSolrConditionInternal(NullFilter it)      '''-«fieldName»:[* TO *]'''

    def dispatch toSolrConditionInternal(FieldFilter it)     '''+«fieldName»:«toSolr»'''
}
