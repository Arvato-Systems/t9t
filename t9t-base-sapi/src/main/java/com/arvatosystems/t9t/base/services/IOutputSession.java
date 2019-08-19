/*
 * Copyright (c) 2012 - 2018 Arvato Systems GmbH
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
import de.jpaw.bonaparte.pojos.api.media.MediaXType;

/** represents a single session within the executor (which spans multiple requests) */

public interface IOutputSession extends AutoCloseable {

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
    public Long open(OutputSessionParameters params);

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

    /** Returns the use file of queue name (if applicable). */
    String getFileOrQueueName();

    /** Returns true if the record transformer is known not to support DataWithTracking. Should be used to
     * extract the data part for variable outputs.
     * Accepts any setting done for the OutputSessionParameters.
     */
    boolean getUnwrapTracking(Boolean ospSetting);

    /** Returns a generic configuration settings for the output session. */
    Object getZ(String key);
}
