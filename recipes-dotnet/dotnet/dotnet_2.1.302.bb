DESCRIPTION = "The .NET Core runtime and SDK"
HOMEPAGE = "http://dot.net/"
LICENSE = "MIT"
SECTION = "devel"

LIC_FILES_CHKSUM = "file://LICENSE.txt;md5=42b611e7375c06a28601953626ab16cb"

SRC_URI = "https://download.microsoft.com/download/4/0/9/40920432-3302-47a8-b13c-bbc4848ad114/dotnet-sdk-${PV}-linux-x64.tar.gz;destdir=dotnet-sdk;destsuffix=test"
SRC_URI[md5sum] = "ff6745447436e30a29f054c4c3d84157"
SRC_URI[sha256sum] = "2acaed79dfb54afd583a6316be63c4e497bad401e96477e4182a35960c4e1fa9"

S = "${WORKDIR}/dotnet-sdk-${PV}-linux-x64"

python base_do_unpack() {
    src_uri = (d.getVar('SRC_URI') or "").split()
    if len(src_uri) == 0:
        return

    try:
        fetcher = bb.fetch2.Fetch(src_uri, d)
        fetcher.unpack(d.getVar('WORKDIR') + "/dotnet-sdk-" + d.getVar("PV") + "-linux-x64")
    except bb.fetch2.BBFetchException as e:
        bb.fatal(str(e))
}

INSANE_SKIP_${PN} += "already-stripped file-rdeps staticdev"
SKIP_FILEDEPS_${PN} = '1'

do_install() {
	install -d ${D}${datadir}/dotnet
	cp -rp ${S}/* ${D}${datadir}/dotnet
    install -d ${D}${bindir}
    ln -s ../..${datadir}/dotnet/dotnet ${D}${bindir}/dotnet
}