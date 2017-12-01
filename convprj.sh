#! /bin/bash
if [ ! -d $1/device/hisilicon/bigfish/packages/apps/HsLauncher ]
then
	mkdir $1/device/hisilicon/bigfish/packages/apps/HsLauncher
fi
rsync -a --delete app/src/main/java/ $1/device/hisilicon/bigfish/packages/apps/HsLauncher/src/
rsync -a --delete app/src/main/res/ $1/device/hisilicon/bigfish/packages/apps/HsLauncher/res/
rsync -a app/src/main/AndroidManifest.xml $1/device/hisilicon/bigfish/packages/apps/HsLauncher/AndroidManifest.xml
cat>$1/device/hisilicon/bigfish/packages/apps/HsLauncher/Android.mk<<"_EOF"
LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_PACKAGE_NAME := Gecko_Launcher
LOCAL_CERTIFICATE := testkey
LOCAL_STATIC_JAVA_LIBRARIES += SDKInvoke
LOCAL_OVERRIDES_PACKAGES := platform
LOCAL_PROGUARD_ENABLED := disabled
ALL_DEFAULT_INSTALLED_MODULES += $(LOCAL_PACKAGE_NAME)
include $(BUILD_PACKAGE)
_EOF
