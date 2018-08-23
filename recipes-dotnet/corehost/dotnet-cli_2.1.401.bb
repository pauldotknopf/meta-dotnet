DESCRIPTION = ".NET CLI (SDK + runtime)"
HOMEPAGE = "http://dot.net/"
LICENSE = "MIT"
SECTION = "devel"

SRC_URI = "git://github.com/dotnet/cli.git;nobranch=1"
SRCREV = "v2.1.401"
LIC_FILES_CHKSUM = "file://LICENSE;md5=42b611e7375c06a28601953626ab16cb"
S = "${WORKDIR}/git"

do_compile() {
    cd ${S}
    export CLIBUILD_SKIP_TESTS=true
    export DropSuffix=true
    ./build.sh --configuration Release \
        --architecture x64 \
        --runtime-id linux-x64
}

do_install() {
    install -d ${D}${datadir}/dotnet
    tar -xvpf ${S}/bin/2/linux-x64/packages/dotnet-sdk-2.1.401-linux-x64.tar.gz -C ${D}${datadir}/dotnet
}