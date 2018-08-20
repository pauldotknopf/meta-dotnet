CORE_SETUP_SRCREV = "yocto-v${PV}"

include corehost.inc

do_compile() {
    cd ${S}
    # Bitbake sets bindir ("/usr/bin") which MsBuild would happily pick up
    # as BinDir to store the built libraries in
    unset bindir
    export ROOTFS_DIR=${STAGING_DIR_HOST}
    export CONFIG_DIR=${WORKDIR}/cmake-config
    export OverridePackageSource="${STAGING_DIR_HOST}/opt/dotnet-nupkg"
    ./build.sh \
        -ConfigurationGroup=Release \
        -TargetArchitecture=x64 \
        -PortableBuild=true \
        -SkipTests=true \
        -DisableCrossgen=true \
        -CrossBuild=true
}

do_install() {
    install -d ${D}/opt/dotnet
    cp -dr ${S}/Bin/obj/linux-x64.Release/combined-framework-host/* ${D}/opt/dotnet/
}