/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.felix.webconsole.internal.configuration;

import java.io.PrintWriter;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.felix.webconsole.internal.AbstractConfigurationPrinter;
import org.apache.felix.webconsole.internal.misc.ConfigurationRender;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * ConfigurationAdminConfigurationPrinter uses the {@link ConfigurationAdmin} service
 * to print the available configurations.
 */
public class ConfigurationAdminConfigurationPrinter extends AbstractConfigurationPrinter
{

    private static final String TITLE = "Configurations";

    /**
     * @see org.apache.felix.webconsole.ConfigurationPrinter#getTitle()
     */
    @Override
    public String getTitle()
    {
        return TITLE;
    }

    /**
     * @see org.apache.felix.webconsole.ConfigurationPrinter#printConfiguration(java.io.PrintWriter)
     */
    @Override
    public void printConfiguration(PrintWriter pw)
    {
        ServiceReference sr = getBundleContext().getServiceReference( ConfigManager.CONFIGURATION_ADMIN_NAME );
        if (sr == null)
        {
            pw.println("Status: Configuration Admin Service not available");
        }
        else
        {

            ConfigurationAdmin ca = (ConfigurationAdmin) getBundleContext().getService(sr);
            try
            {
                Configuration[] configs = ca.listConfigurations(null);

                if (configs != null && configs.length > 0)
                {
                    Set factories = new HashSet();
                    SortedMap sm = new TreeMap();
                    for (int i = 0; i < configs.length; i++)
                    {
                        sm.put(configs[i].getPid(), configs[i]);
                        String fpid = configs[i].getFactoryPid();
                        if (null != fpid)
                        {
                            factories.add(fpid);
                        }
                    }
                    if (factories.isEmpty())
                    {
                        pw.println("Status: " + configs.length
                            + " configurations available");
                    }
                    else
                    {
                        pw.println("Status: " + configs.length + " configurations with " + factories.size()
                                + " different factories available");
                    }
                    pw.println();

                    for (Iterator mi = sm.values().iterator(); mi.hasNext();)
                    {
                        this.printConfiguration(pw, (Configuration) mi.next());
                    }
                }
                else
                {
                    pw.println("Status: No Configurations available");
                }
            }
            catch (Exception e)
            {
                pw.println("Status: Configuration Admin Service not accessible");
            }
            finally
            {
                getBundleContext().ungetService(sr);
            }
        }
    }

    private void printConfiguration(PrintWriter pw, Configuration config)
    {
        ConfigurationRender.infoLine(pw, "", "PID", config.getPid());

        if (config.getFactoryPid() != null)
        {
            ConfigurationRender.infoLine(pw, "  ", "Factory PID", config.getFactoryPid());
        }

        String loc = (config.getBundleLocation() != null) ? config.getBundleLocation()
            : "Unbound";
        ConfigurationRender.infoLine(pw, "  ", "BundleLocation", loc);

        Dictionary props = config.getProperties();
        if (props != null)
        {
            SortedSet keys = new TreeSet();
            for (Enumeration ke = props.keys(); ke.hasMoreElements();)
            {
                keys.add(ke.nextElement());
            }

            for (Iterator ki = keys.iterator(); ki.hasNext();)
            {
                String key = (String) ki.next();
                ConfigurationRender.infoLine(pw, "  ", key, props.get(key));
            }
        }

        pw.println();
    }

}
