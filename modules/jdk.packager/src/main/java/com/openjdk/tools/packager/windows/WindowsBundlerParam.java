/*
 * Copyright (c) 2014, 2015, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.openjdk.tools.packager.windows;

import com.openjdk.tools.packager.BundlerParamInfo;
import com.openjdk.tools.packager.JreUtils;
import com.openjdk.tools.packager.StandardBundlerParam;
import com.openjdk.tools.packager.RelativeFileSet;
import com.sun.openjfx.tools.packager.bundlers.BundleParams;

import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.openjdk.tools.packager.JreUtils.extractJreAsRelativeFileSet;

public class WindowsBundlerParam<T> extends StandardBundlerParam<T> {

    private static final ResourceBundle I18N = ResourceBundle.getBundle(WindowsBundlerParam.class.getName());

    public WindowsBundlerParam(String name, String description, String id, Class<T> valueType, Function<Map<String, ? super Object>, T> defaultValueFunction, BiFunction<String, Map<String, ? super Object>, T> stringConverter) {
        super(name, description, id, valueType, defaultValueFunction, stringConverter);
    }

    public static final BundlerParamInfo<String> INSTALLER_FILE_NAME = new StandardBundlerParam<> (
            I18N.getString("param.installer-name.name"),
            I18N.getString("param.installer-name.description"),
            "win.installerName",
            String.class,
            params -> {
                String nm = APP_NAME.fetchFrom(params);
                if (nm == null) return null;

                String version = VERSION.fetchFrom(params);
                if (version == null) {
                    return nm;
                } else {
                    return nm + "-" + version;
                }
            },
            (s, p) -> s);

    public static final BundlerParamInfo<String> APP_REGISTRY_NAME = new StandardBundlerParam<> (
            I18N.getString("param.registry-name.name"),
            I18N.getString("param.registry-name.description"),
            "win.registryName",
            String.class,
            params -> {
                String nm = APP_NAME.fetchFrom(params);
                if (nm == null) return null;

                return nm.replaceAll("[^-a-zA-Z\\.0-9]", "");
            },
            (s, p) -> s);

    public static final StandardBundlerParam<String> MENU_GROUP =
            new StandardBundlerParam<>(
                    I18N.getString("param.menu-group.name"),
                    I18N.getString("param.menu-group.description"),
                    "win.menuGroup",
                    String.class,
                    params -> params.containsKey(VENDOR.getID())
                            ? VENDOR.fetchFrom(params)
                            : params.containsKey(CATEGORY.getID())
                            ? CATEGORY.fetchFrom(params)
                            : I18N.getString("param.menu-group.default"),
                    (s, p) -> s
            );

    public static final StandardBundlerParam<Boolean> BIT_ARCH_64 =
            new StandardBundlerParam<>(
                    I18N.getString("param.64-bit.name"),
                    I18N.getString("param.64-bit.description"),
                    "win.64Bit",
                    Boolean.class,
                    params -> System.getProperty("os.arch").contains("64"),
                    (s, p) -> Boolean.valueOf(s)
            );

    public static final StandardBundlerParam<Boolean> BIT_ARCH_64_RUNTIME =
            new StandardBundlerParam<>(
                    I18N.getString("param.runtime-64-bit.name"),
                    I18N.getString("param.runtime-64-bit.description"),
                    "win.64BitJreRuntime",
                    Boolean.class,
                    params -> {WinAppBundler.extractFlagsFromRuntime(params); return "64".equals(params.get(".runtime.bit-arch"));},
                    (s, p) -> Boolean.valueOf(s)
            );

    //Subsetting of JRE is restricted.
    //JRE README defines what is allowed to strip:
    //   http://www.oracle.com/technetwork/java/javase/jre-8-readme-2095710.html
    public static final BundlerParamInfo<JreUtils.Rule[]> WIN_JRE_RULES = new StandardBundlerParam<>(
            "",
            "",
            ".win.runtime.rules",
            JreUtils.Rule[].class,
            params -> new JreUtils.Rule[]{
                    JreUtils.Rule.prefixNeg("\\bin\\new_plugin"),
                    JreUtils.Rule.prefixNeg("\\lib\\deploy"),
                    JreUtils.Rule.suffixNeg(".pdb"),
                    JreUtils.Rule.suffixNeg(".map"),
                    JreUtils.Rule.suffixNeg("axbridge.dll"),
                    JreUtils.Rule.suffixNeg("eula.dll"),
                    JreUtils.Rule.substrNeg("javacpl"),
                    JreUtils.Rule.suffixNeg("wsdetect.dll"),
                    JreUtils.Rule.substrNeg("eployjava1.dll"), //NP and IE versions
                    JreUtils.Rule.substrNeg("bin\\jp2"),
                    JreUtils.Rule.substrNeg("bin\\jpi"),
                    //Rule.suffixNeg("lib\\ext"), //need some of jars there for https to work
                    JreUtils.Rule.suffixNeg("ssv.dll"),
                    JreUtils.Rule.substrNeg("npjpi"),
                    JreUtils.Rule.substrNeg("npoji"),
                    JreUtils.Rule.suffixNeg(".exe"),
                    //keep core deploy files as JavaFX APIs use them
                    //Rule.suffixNeg("deploy.dll"),
                    JreUtils.Rule.suffixNeg("deploy.jar"),
                    //Rule.suffixNeg("javaws.jar"),
                    //Rule.suffixNeg("plugin.jar"),
                    JreUtils.Rule.suffix(".jar")
            },
            (s, p) -> null
    );

    public static final BundlerParamInfo<RelativeFileSet> WIN_RUNTIME = new StandardBundlerParam<>(
            I18N.getString("param.runtime.name"),
            I18N.getString("param.runtime.description"),
            BundleParams.PARAM_RUNTIME,
            RelativeFileSet.class,
            params -> extractJreAsRelativeFileSet(System.getProperty("java.home"),
                    WIN_JRE_RULES.fetchFrom(params)),
            (s, p) -> extractJreAsRelativeFileSet(s,
                    WIN_JRE_RULES.fetchFrom(p))
    );

    public static final BundlerParamInfo<Boolean> INSTALLDIR_CHOOSER = new StandardBundlerParam<> (
        I18N.getString("param.installdir-chooser.name"),
        I18N.getString("param.installdir-chooser.description"),
        BundleParams.PARAM_INSTALLDIR_CHOOSER,
        Boolean.class,
        params -> Boolean.FALSE,
        (s, p) -> Boolean.valueOf(s)
    );

}
