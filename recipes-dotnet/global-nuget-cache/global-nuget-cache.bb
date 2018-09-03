DESCRIPTION = "A global cache of shared NuGet packages."
HOMEPAGE = "http://dot.net/"
LICENSE = "MIT"
SECTION = "devel"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Proprietary;md5=0557f9d92cf58f2ccdd50f62f8ac0b28"

SRC_URI += "file://Packages.csproj"

inherit dotnetnative

do_install() {
    install -d ${D}/opt/nuget

    dotnet store \
        --skip-optimization \
        --skip-symbols \
        --output ${D}/opt/nuget \
        --manifest ${WORKDIR}/Packages.csproj \
        --framework netcoreapp2.1 \
        --runtime linux-x64 \
        --verbosity diag
}

FILES_${PN} += "/opt/nuget"