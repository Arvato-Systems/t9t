/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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
package com.arvatosystems.t9t.base.services;

import java.io.OutputStream;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.output.OutputSessionParameters;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.bonaparte.pojos.api.media.MediaXType;
import jakarta.annotation.Nullable;

/** represents a single session within the executor (which spans multiple requests) */

public interface IOutputSession extends AutoCloseable {
    String NO_PARTITION_KEY = "";
    String NO_RECORD_KEY = "";

    /**
     * open() must be called exactly once, immediately after instancing a new object of type IOutputSession. The productive implementation creates an entry in
     * the p42_dat_sinks table, based on parameters and configuration from table p42_cfg_data_sinks. It then opens either the output file or connects to the JMS
     * queue.
     *
     * @param params
     *            parameters identifying the output
     * @return the artificial primary key of the new entry in table p42_dat_sinks
     * @throws T9tException
     */
    Long open(OutputSessionParameters params);

    /**
     * Method to return the created OutputStream in case of file based output. If this method is used, binary data such as PDF or XLS documents can be written
     * into the data sink directly by the caller, reusing all file based logging. It should not be mixed with structured output using the store() method. The
     * method returns null if this sink has been configured to write to a JMS queue. A future extension may implement writing binary data to the stream.
     *
     * @return the java.io.OutputStream
     * @throws T9tException
     *             if the IOutputSession is not open.
     */
    OutputStream getOutputStream();

    /**
     * Method to return the used communication / file format. To be used for outputs which do not have their own configuration. Applications using this call
     * will usually leave the MediaType unspecified in the params passed to open().
     *
     * @return the MediaType as determined by combining the database configuration with parameters supplied by open()
     * @throws T9tException
     *             if the IOutputSession is not open.
     */
    MediaXType getCommunicationFormatType();

    /**
     * storeCustomElement() is supported for very few export formats only. It writes a special data object to the output stream.
     * This is currently supported for XML exports only.
     *
     * @param name the tag of the object
     * @param value the value
     *
     * @throws T9tException
     */
    void storeCustomElement(String name, Object value);

    /**
     * store() is called for every record of structured data to be written. It creates a logging entry into table base_int_outbound_messages and then writes the
     * message either to the file or to the JMS queue.
     *
     * @param record
     *            the data record to be written.
     * @throws T9tException
     */
    void store(BonaPortable record);

    /**
     * store() is called for every record of structured data to be written. It creates a logging entry into table base_int_outbound_messages and then writes the
     * message either to the file or to the JMS queue.
     *
     * @param recordRef
     *            the record reference
     * @param record
     *            the data record to be written.
     * @throws T9tException
     */
    void store(Long recordRef, BonaPortable record);

    /**
     * store() is called for every record of structured data to be written. It creates a logging entry into table base_int_outbound_messages and then writes the
     * message either to the file or to the JMS queue or Kafka. This is the preferred format, because it transmits the partitioning information.
     *
     * @param recordRef
     *            the record reference
     * @param record
     *            the data record to be written.
     * @throws T9tException
     */
    void store(Long recordRef, String partitionKey, String recordKey, BonaPortable record);

    /**
     * Method to terminate the output.
     *
     * @throws T9tException
     *             if any exception occurred while trying to close the file or queue.
     */
    @Override
    void close() throws Exception;

    /** Returns the maximum number of records for a single output batch, or null if no limit has been configured. */
    Integer getMaxNumberOfRecords();

    /** Returns the chunk size as configured. */
    Integer getChunkSize();

    /**
     * Returns the use file of queue name (if applicable).
     * @Deprecated, because it should not be used to write to the output session (use <code>getOutputStream()</code> for that),
     * and as a reference after writing is complete, the new method getReferenceMediaData() will provide more information,
     * such as also the actual format type, and the specification of the storage type, if the output is suitable for later retrieval.
     */
    @Deprecated
    String getFileOrQueueName();

    /** Returns true if the record transformer is known not to support DataWithTracking. Should be used to
     * extract the data part for variable outputs.
     * Accepts any setting done for the OutputSessionParameters.
     */
    boolean getUnwrapTracking(Boolean ospSetting);

    /** Returns a generic configuration settings for the output session. */
    Object getZ(String key);

    /**
     * Returns a MediaData object which can be used for lazy references to the stored data.
     * Will throw an exception if the output is not yet complete.
     * Will return null, if the output target is not suitable for later retrieval (for example some queue or topic), or if the output was empty.
     */
    @Nullable MediaData getReferenceMediaData();
}
