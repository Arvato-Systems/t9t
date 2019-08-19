#!/bin/bash
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

echo "Please enter your password - this needs to go into the t9t server configuration file, in entry keyStorePassword"
read password
echo "Creating a new keystore with password " $password
rm t9tkeystore.jceks
keytool -genseckey -keystore t9tkeystore.jceks -storetype jceks -storepass $password -keyalg HMacSHA256 -keysize 2048 -alias HS256 -keypass $password
keytool -genseckey -keystore t9tkeystore.jceks -storetype jceks -storepass $password -keyalg HMacSHA384 -keysize 2048 -alias HS384 -keypass $password
keytool -genseckey -keystore t9tkeystore.jceks -storetype jceks -storepass $password -keyalg HMacSHA512 -keysize 2048 -alias HS512 -keypass $password
keytool -genkey -keystore t9tkeystore.jceks -storetype jceks -storepass $password -keyalg RSA -keysize 2048 -alias RS256 -keypass $password -sigalg SHA256withRSA -dname "CN=,OU=,O=,L=,ST=,C=" -validity 360
keytool -genkey -keystore t9tkeystore.jceks -storetype jceks -storepass $password -keyalg RSA -keysize 2048 -alias RS384 -keypass $password -sigalg SHA384withRSA -dname "CN=,OU=,O=,L=,ST=,C=" -validity 360
keytool -genkey -keystore t9tkeystore.jceks -storetype jceks -storepass $password -keyalg RSA -keysize 2048 -alias RS512 -keypass $password -sigalg SHA512withRSA -dname "CN=,OU=,O=,L=,ST=,C=" -validity 360
keytool -genkeypair -keystore t9tkeystore.jceks -storetype jceks -storepass $password -keyalg EC -keysize 256 -alias ES256 -keypass $password -sigalg SHA256withECDSA -dname "CN=,OU=,O=,L=,ST=,C=" -validity 360
keytool -genkeypair -keystore t9tkeystore.jceks -storetype jceks -storepass $password -keyalg EC -keysize 256 -alias ES384 -keypass $password -sigalg SHA384withECDSA -dname "CN=,OU=,O=,L=,ST=,C=" -validity 360
keytool -genkeypair -keystore t9tkeystore.jceks -storetype jceks -storepass $password -keyalg EC -keysize 256 -alias ES512 -keypass $password -sigalg SHA512withECDSA -dname "CN=,OU=,O=,L=,ST=,C=" -validity 360

echo "Your generated key store is now available. Put it onto your server and configure the path to it in the t9t server configuration file, in entry keyStorePath"
