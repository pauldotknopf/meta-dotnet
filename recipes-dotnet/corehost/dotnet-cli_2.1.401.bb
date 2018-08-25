DESCRIPTION = ".NET CLI (SDK + runtime)"
HOMEPAGE = "http://dot.net/"
LICENSE = "MIT"
SECTION = "devel"

FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

SRC_URI = "git://github.com/dotnet/cli.git;nobranch=1 \
    file://0001-use-production-version-id-of-core-app.patch"
SRCREV = "v2.1.401"
LIC_FILES_CHKSUM = "file://LICENSE;md5=42b611e7375c06a28601953626ab16cb"
S = "${WORKDIR}/git"

DEPENDS += "corehost"
RDEPENDS_${PN} = "libicuuc libicui18n libcurl libuv libssl"

do_compile() {
    cd ${S}

    # Create a link to our own compiled runtime. The build script will ignore downloading
    # if it already exists.
    rm -rf ${S}/bin/2/linux-x64/intermediate/coreSetupDownload/2.1.3/combinedSharedHostAndFrameworkArchive.tar.gz
    rm -rf ${S}/bin/2/linux-x64/intermediate/sharedFrameworkPublish
    mkdir -p ${S}/bin/2/linux-x64/intermediate/coreSetupDownload/2.1.3
    mkdir -p ${S}/bin/2/linux-x64/intermediate/sharedFrameworkPublish
    ln -s ${STAGING_DIR_HOST}/opt/dotnet-tarballs/dotnet-runtime-2.1.3-linux-x64.tar.gz ${S}/bin/2/linux-x64/intermediate/coreSetupDownload/2.1.3/combinedSharedHostAndFrameworkArchive.tar.gz
    tar xf ${STAGING_DIR_HOST}/opt/dotnet-tarballs/dotnet-runtime-2.1.3-linux-x64.tar.gz -C ${S}/bin/2/linux-x64/intermediate/sharedFrameworkPublish

    export CLIBUILD_SKIP_TESTS=true
    export DropSuffix=true
    export DISABLE_CROSSGEN=true
    ./build.sh --configuration Release \
        --architecture x64 \
        --runtime-id linux-x64
}

INSANE_SKIP_${PN} += "already-stripped dev-so staticdev libdir"

do_install() {
    install -d ${D}${datadir}/dotnet
    tar -xvpf ${S}/bin/2/linux-x64/packages/dotnet-sdk-2.1.401-linux-x64.tar.gz -C ${D}${datadir}/dotnet
}

FILES_${PN} = "${datadir}/dotnet"