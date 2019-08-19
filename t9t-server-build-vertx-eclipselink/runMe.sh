#
# Copyright (c) 2012 - 2018 Arvato Systems GmbH
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# example how to invoke the server with additional JARs.
# In this case, we cannot use the -jar option, because the Manifest then overrides any additional classpath parameters

java -javaagent:/home/mbi/java/eclipselink/jlib/eclipselink.jar \
    -cp ../t9t-demo-api/target/t9t-demo-api-2.5.0-SNAPSHOT.jar:../t9t-demo-be/target/t9t-demo-be-2.5.0-SNAPSHOT.jar:target/t9t-server.jar \
    com.arvatosystems.t9t.server.Main -T ACME -U mbi
