DESCRIPTION = ".NET Core Libraries (CoreFX)"
HOMEPAGE = "http://dot.net/"
LICENSE = "MIT"
SECTION = "devel"

DEPENDS = "clang-native libunwind gettext icu openssl util-linux cmake-native krb5 curl ca-certificates-native"
RDEPENDS_${PN} = "libcurl libuv"

SRC_URI = "git://github.com/qmlnet/corefx.git;nobranch=1 \
"

SRCREV = "${COREFX_SRCREV}"
LIC_FILES_CHKSUM = "file://LICENSE.TXT;md5=9fc642ff452b28d62ab19b7eea50dfb9"
S = "${WORKDIR}/git"

inherit dotnet_cmake

# Install the stripped binaries, the unstripped are smaller and causes Bus error
INSANE_SKIP_${PN} += "already-stripped dev-so"

include base.inc

do_configure() {
	cd ${S}
	./init-tools.sh
}

do_compile() {
	cd ${S}
	
	# Bitbake sets bindir ("/usr/bin") which MsBuild would happily pick up
	# as BinDir to store the built libraries in
	unset bindir

    export CONFIG_DIR=${WORKDIR}/cmake-config
	export ROOTFS_DIR=${STAGING_DIR_HOST}
    export GCC_TOOLCHAIN=${STAGING_BINDIR_TOOLCHAIN} 
    
    ./run.sh build-native -SkipTests -release -buildArch=x64 -- verbose cross
	
	
	./run.sh build-managed -BuildPackages=true -SkipTests -release -buildArch=x64 -runtimeos=linux -BuildNumberMajor=${COREFX_BUILD_MAJOR} -BuildNumberMinor=${COREFX_BUILD_MINOR}

	chrpath -d bin/Linux.x64.Release/native/System.IO.Compression.Native.so
	chrpath -d bin/Linux.x64.Release/native/System.Net.Security.Native.so
	chrpath -d bin/Linux.x64.Release/native/System.Net.Http.Native.so
}

do_install() {
	export src="${S}/bin"
	export target="${D}/opt/dotnet/"

	install -d ${target}

	# Install the stripped binaries, the unstripped are smaller and causes Bus error
	for i in `ls ${src}/Linux.x64.Release/native/*.so`
	do
		install -m 0755 ${i} ${target}
	done

	# This will overwrite a AnyOS version with a Linux version etc.
	for arch in AnyOS.AnyCPU.Release Unix.AnyCPU.Release Linux.AnyCPU.Release Linux.x64.Release
	do
		for i in `find ${src}/obj/$arch/ -name "*.dll"`
		do
			install -m 0644 ${i} ${target}
		done
	done

	# Add link so the runtime can find libuv
	ln -s /usr/lib/libuv.so.1 ${target}/libuv.so

	# Add DLLs from Microsofts tools distribution
	for i in ${TOOLS_DLLS}
	do
		install -m 0644 ${S}/Tools/${i} ${target}
	done

	# Create dev package
	install -d ${D}/opt/dotnet-nupkg/
	for i in `ls ${src}/packages/Release/*.nupkg`
	do
		install -m 0644 ${i} ${D}/opt/dotnet-nupkg/
	done

	# Publish nuget packages
	for i in `ls ${S}/packages/*/*/*.nupkg`
	do
		install -m 0644 ${i} ${D}/opt/dotnet-nupkg/
	done
}

SKIP_FILEDEPS_${PN} = "1"

FILES_${PN} = "/opt/dotnet"
FILES_${PN}-dev = "/opt/dotnet-nupkg"

TOOLS_DLLS = "\
  Microsoft.CodeAnalysis.CSharp.dll \
  Microsoft.CodeAnalysis.dll \
  Microsoft.CodeAnalysis.VisualBasic.dll \
"

sysroot_stage_all_append () {
    sysroot_stage_dir ${D}/opt/dotnet-nupkg ${SYSROOT_DESTDIR}/opt/dotnet-nupkg
}