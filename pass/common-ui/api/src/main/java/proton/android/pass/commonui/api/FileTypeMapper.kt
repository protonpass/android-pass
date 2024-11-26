/*
 * Copyright (c) 2024 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.commonui.api

import android.webkit.MimeTypeMap

enum class FileType(val extensions: Set<String>) {
    RasterImage(
        setOf(
            "2bp", "8ci", "8pbs", "accountpicture-ms", "afphoto", "apng", "aps", "ase",
            "aseprite", "avatar", "avifs", "awd", "bif", "blz", "bmp", "bmq", "bpg", "bruh", "cals", "can", "cdc",
            "cdg", "clip", "clip", "cpc", "cpd", "ct", "ctex", "dds", "dgt", "dib", "djvu", "drz", "dtw", "ecw", "exr",
            "fac", "fbm", "ff", "fil", "fits", "flif", "fpx", "g3", "gif", "gih", "gim", "hdr", "heic", "heif", "i3d",
            "icn", "icon", "ipick", "ipv", "itc2", "iwi", "j2k", "jbig2", "jls", "jng", "jpc", "jpe", "jpeg", "jpf",
            "jpg", "jps", "jxl", "kdk", "kfx", "kra", "ktx", "l73i", "lb", "linea", "lip", "lrpreview", "lsa", "lzp",
            "mdp", "mng", "mnr", "mpf", "mpo", "msp", "nlm", "nol", "oc4", "oplc", "ota", "otb", "ozj", "pam", "pat",
            "pbm", "pcx", "pdd", "pdn", "pfi", "pgm", "pi2", "pic", "piskel", "pixela", "pm", "pmg", "pnc", "png",
            "pni", "pns", "pov", "ppf", "ppm", "ppp", "psb", "psd", "psdc", "psp", "pspimage", "ptex", "ptg", "pwp",
            "px", "pxd", "pzp", "qoi", "rpf", "rsr", "sai", "six", "skitch", "sktz", "sld", "snag", "snagx", "spa",
            "spp", "sprite", "sprite2", "sprite3", "sr", "stex", "sumo", "tbn", "tfc", "tg4", "tga", "thm", "tif",
            "tiff", "tm2", "tn", "tn3", "tpf", "urt", "usertile-ms", "vicar", "viff", "vna", "vpe", "vrimg", "vrphoto",
            "wb2", "wbc", "wbmp", "wbz", "webp", "wic", "wp2", "xbm", "xcf", "xpm", "ysp"
        )
    ),
    VectorImage(
        setOf(
            "ac5", "ac6", "af2", "af3", "afdesign", "ai", "aic", "ait", "amdn", "apm",
            "art", "artb", "asy", "awg", "cag", "ccx", "cdd", "cddz", "cdmm", "cdmt", "cdmtz", "cdmz", "cdr", "cdrapp",
            "cdsx", "cdt", "cdtx", "cdtz", "cdx", "cdx", "cgm", "cil", "clarify", "cmx", "cnv", "cor", "csy", "curve",
            "cv5", "cvd", "cvdtpl", "cvg", "cvi", "cvs", "cvx", "cvxcad", "cwt", "dcs", "ddrw", "design", "dhs", "dia",
            "dpp", "dpr", "dpx", "drawing", "drawio", "drawit", "drw", "ds4", "dsg", "dxb", "ecs5", "egc", "emf",
            "emz", "ep", "epgz", "eps", "epsf", "esc", "ezdraw", "fcm", "fh10", "fh11", "fh3", "fh4", "fh5", "fh6",
            "fh7", "fh8", "fh9", "fhd", "fif", "fmv", "fs", "ft10", "ft11", "ft7", "ft8", "ft9", "ftn", "fxg", "gem",
            "gks", "glox", "gls", "graffle", "gsd", "gstencil", "gtemplate", "gvdesign", "hgl", "hpg", "hpgl", "hpl",
            "hvif", "igt", "igx", "imd", "ink", "isf", "jsl", "lmk", "maker", "mgc", "mgcb", "mgmf", "mgmt", "mgmx",
            "mgs", "mgtx", "mmat", "mvg", "ndb", "ndtx", "ndx", "nodes", "odg", "otg", "ovp", "ovr", "p", "pcs", "pct",
            "pd", "pen", "pfd", "pfv", "pict", "pixil", "pl", "plt", "pobj", "ps", "psid", "puppet", "pws", "qcc",
            "rdl", "scut5", "scv", "sda", "shapes", "sk1", "sk2", "sketch", "sketchpad", "slddrt", "snagstyles", "ssk",
            "std", "stn", "svf", "svg", "svgz", "svm", "sxd", "tex.emz", "tlc", "tne", "tpl", "ufr", "vbr", "vec",
            "vectornator", "vml", "vsd", "vsdm", "vsdx", "vst", "vstm", "vstx", "wmf", "wmz", "wpg", "wpi", "xar",
            "xmmap", "xmmat", "yal", "ydr", "ylc", "zgm", "zmf"
        )
    ),
    Text(
        setOf(
            "1st", "602", "_doc", "_docx", "abw", "act", "adoc", "aim", "ans", "appodeal",
            "apt", "asc", "ascii", "aty", "awp", "awt", "aww", "b", "bad", "bbs", "bdp", "bdr", "bean", "bib",
            "bib", "bibtex", "bloonset", "bml", "bna", "boc", "brx", "btd", "btxt", "bwd", "bxt", "calca", "cast",
            "cec", "charset", "chord", "cnm", "cod", "copf", "crwl", "cws", "cyi", "dat", "dca", "del", "description",
            "dfti", "dgs", "diz", "dne", "dnp", "doc", "docm", "docx", "docxml", "docz", "dotm", "dotx", "dropbox",
            "dsc", "dtex", "dvi", "dwd", "dx", "ebp", "eio", "eit", "embed", "eml", "emlx", "emulecollection", "err",
            "etf", "etx", "euc", "fadein.template", "faq", "fbl", "fcf", "fdf", "fdr", "fdt", "fdx", "fdxt", "fft",
            "flr", "fluid", "fodt", "fountain", "fpt", "fwdn", "gdoc", "gform", "gjam", "gmap", "gpd", "gpn", "graph",
            "gscript", "gsite", "gslides", "gtable", "gv", "hbk", "hht", "highland", "hs", "hwp", "hz", "idx", "ipf",
            "ipspot", "ipynb", "jarvis", "jdl", "jis", "jlqm", "jnp", "joe", "jp1", "jtd", "kes", "klg", "knt", "kwd",
            "latex", "license", "lis", "lnk42", "lnt", "loop", "lp2", "lst", "ltr", "ltx", "ltxd", "lue", "luf", "lwp",
            "lxfml", "lyt", "lyx", "man", "mbox", "md", "me", "mell", "mellel", "mpd", "msg", "msr", "mss", "mw",
            "mwd", "mwp", "nb", "ndoc", "nfo", "ngloss", "nisus", "njk", "njx", "note", "notes", "now", "nwctxt",
            "nwm", "nwp", "ocr", "odif", "odm", "odo", "odt", "ofl", "omfl", "opeico", "openbsd", "org", "ort",
            "ott", "p7s", "pages", "pages-tef", "pbj", "pimx", "plain", "plantuml", "pmo", "prt", "psw", "psx", "pu",
            "pvj", "pvm", "pwd", "pwdp", "pwdpl", "pwi", "pwr", "qbl", "qdl", "qpf", "qpqd", "quid", "readme", "rft",
            "ris", "rpt", "rst", "rtd", "rtf", "rtfd", "rtx", "rvf", "rzn", "safetext", "sam", "save", "scc", "scm",
            "scriv", "scrivx", "sdm", "sdw", "se", "session", "sgm", "shim", "sla", "sla.gz", "sms", "sp1", "story",
            "stp", "strings", "stw", "sty", "sublime-project", "sublime-workspace", "sxg", "sxw", "tab", "tdf", "tex",
            "text", "textclipping", "tfrproj", "tm", "tmdx", "tmvx", "tnl", "tpc", "trelby", "tvj", "txt", "typ",
            "u3i", "udd", "unauth", "unx", "uof", "uot", "upd", "utf8", "utxt", "vcf", "vnt", "vwr", "wbk", "webdoc",
            "wp", "wp4", "wp5", "wp6", "wp7", "wpa", "wpd", "wpl", "wps", "wpt", "wpw", "wri", "wsp", "wtt", "wtx",
            "xbdoc", "xbplate", "xdf", "xdl", "xwp", "xwp", "xy", "xy3", "xyp", "xyw", "ytdl", "zrtf", "zw", "zzq",
            "zzs"
        )
    ),
    Audio(
        setOf(
            "1te", "1ti", "3ga", "5xe", "6cm", "a2t", "aa3", "aaxc", "abc", "abm", "ac3",
            "acd", "acd-zip", "acm", "acp", "afc", "akp", "alac", "alc", "als", "amxd", "aob", "apf", "ariax", "asd",
            "asdt", "at3", "au", "aup", "awb", "awb", "bcs", "bonk", "bp", "capobundle", "cdo", "cel", "ceol", "cfxr",
            "cgrp", "cidb", "ckb", "copy", "cts", "cue", "cvsd", "cwb", "d00", "dct", "df2", "dfc", "djl", "djp",
            "dmse", "drg", "dsm", "dtm", "dw", "ec3", "efe", "efk", "efs", "efv", "emx", "erb", "f32", "f3r", "fev",
            "fla", "flm", "flp", "fms", "fpa", "frg", "fsc", "ftm", "fur", "g726", "gbproj", "gbs", "gdm", "gio", "gm",
            "gp", "gpk", "gsf", "gsflib", "gsm", "guit", "h2song", "h3e", "h5s", "hbb", "hca", "hes", "hma", "hmi",
            "igp", "ima", "imf", "ins", "isma", "iti", "jbx", "jo", "ksc", "kt3", "logic", "logicx", "lvp", "m3u",
            "m3u8", "m3up", "m4a", "m4r", "med", "midi", "mini2sf", "minigsf", "minissf", "mka", "mmjproject", "mmlp",
            "mmpz", "mon", "mp_", "mpu", "mscx", "mscz_saving", "msv", "mt2", "mti", "mtm", "mui", "musa", "musicxml",
            "mws", "mxl", "nap", "ncw", "nist", "nki", "nksf", "nrt", "nsf", "nsmp", "nsmpproj", "nst", "oga", "ogg",
            "omg", "orc", "ovex", "ovw", "pandora", "pbf", "pcast", "piximod", "pkf", "pla", "pmpl", "pna", "psf1",
            "psf2", "psm", "ptx", "ptxt", "q1", "q2", "qcp", "ram", "rdvxz", "record", "rex", "rgrp", "rip", "rmi",
            "rmm", "rmx", "rns", "rol", "rsf", "rtm", "s3i", "s3m", "sabl", "sbi", "sc2", "sds", "sdt", "sdx",
            "sequence", "sesx", "sf2", "sfas", "sfl", "sfpack", "sgp", "sib", "sip", "slp", "smf", "smp", "sng",
            "sngx", "sns", "sph", "sseq", "ssflib", "ssp", "stm", "streamdeckaudio", "strm", "sv", "svp", "sw", "swa",
            "syh", "syn", "syw", "syx", "toc", "trak", "u", "u8", "uax", "ub", "ulw", "uni", "ust", "vag", "vb", "vlc",
            "vmf", "voc", "voi", "voxal", "vpw", "vqf", "vsq", "vsqx", "vwp", "vyf", "wand", "wav", "wave", "weba",
            "wfp", "wma", "wproj", "wus", "xi", "xmi", "xmu", "xmz", "xpf", "xrns", "xt", "ym", "ymd", "ymi", "zab",
            "zpa"
        )
    ),
    Video(
        setOf(
            "264", "3g2", "3gp", "3gp2", "3mm", "60d", "890", "aaf", "aec", "aecap", "aep",
            "aepx", "aet", "aetx", "ale", "am", "amc", "amx", "aqt", "arcut", "asf", "av", "av1", "avb", "ave", "avm",
            "avr", "avs", "avtech", "avv", "awlive", "axm", "axp", "axv", "bdmv", "bdt2", "bik", "bik2", "bix", "bk2",
            "bnp", "bs4", "bsf", "bup", "byu", "camproj", "camrec", "camv", "cine", "clpi", "cme", "cmmp", "cmrec",
            "cpi", "cpvc", "cst", "cvc", "d2v", "d3v", "dash", "dav", "db2", "dck", "ddat", "dif", "dir", "divx",
            "dlx", "dmb", "dmsd", "dmsd3d", "dmsm", "dmsm3d", "dmss", "dmx", "dnc", "dpa", "dream", "drp", "dv",
            "dv-avi", "dv4", "dvdmedia", "dvr", "dvr-ms", "dxr", "dzm", "dzp", "dzt", "evo", "exi", "eye", "eyetv",
            "f4f", "f4p", "f4v", "fbr", "fbz", "fcarch", "fcp", "ffd", "flc", "fli", "flv", "flx", "fvt", "g2m",
            "g64", "g64x", "gcs", "gfp", "gifv", "gom", "gts", "gvf", "gvi", "h261", "h264", "h265", "hdmov", "hdv",
            "hevc", "ifo", "imoviemobile", "imovieproject", "imovietrailer", "inp", "int", "ircp", "ism", "ismv",
            "iva", "ivr", "ivs", "izz", "izzy", "jdr", "jmv", "jss", "jts", "jtv", "jv", "kdenlive", "kine",
            "kmproject", "ktn", "kux", "lrv", "lsav", "lsf", "lsx", "lvf", "lvix", "m15", "m1pg", "m1v", "m2t",
            "m2ts", "m4f", "m4s", "m4u", "m4v", "m75", "mani", "mep", "mepx", "meta", "mgv", "mj2", "mjpg", "mk3d",
            "mmp", "mnv", "mob", "mod", "modd", "moff", "moi", "moov", "movie", "mp21", "mp2v", "mp4", "mp4.infovid",
            "mp4v", "mp5", "mpc", "mpe", "mpeg", "mpeg2", "mpeg4", "mpg", "mpg2", "mpg4", "mpl", "mpls", "mproj",
            "mpsub", "mpv", "mpv2", "mqv", "msdvd", "mswmm", "mts", "mtv", "mv", "mvb", "mvd", "mvex", "mvp", "mvy",
            "mxf", "mxv", "mys", "ncor", "ntp", "nut", "objection", "ogm", "ogv", "ogx", "pac", "pclx", "pdrproj",
            "pds", "piv", "playlist", "plot", "plotdoc", "pmf", "ppj", "pro", "pro4dvd", "projector", "proqc",
            "prproj", "psh", "pssd", "psv", "pva", "pz", "qsv", "qtch", "qtl", "qtz", "r3d", "rcd", "rcproject",
            "rcut", "rdb", "rec", "rm", "rmp", "rms", "rmv", "rmvb", "rsx", "rxr", "san", "sbk", "sbt", "scn",
            "screenflow", "sedprj", "seq", "ser", "sfd", "sfera", "sfvidcap", "siv", "smi", "smil", "smv", "spryzip",
            "srt", "ssa", "ssf", "str", "stu", "stx", "sub", "svi", "swf", "swi", "tbc", "theater", "tivo", "tix",
            "tmv", "tod", "tp", "tpd", "trec", "trp", "ts", "tsp", "tsv", "ttml", "tvs", "tvshow", "ty+", "vc1",
            "vcpf", "vcr", "vcv", "vdo", "vdx", "veg", "vem", "vep", "vf", "vfw", "vgz", "vid", "video", "viewlet",
            "vii", "viv", "vlab", "vob", "vp3", "vp6", "vp7", "vpj", "vproj", "vro", "vs4", "vse", "vsp", "vtt", "w32",
            "webm", "wgi", "wlmp", "wm", "wmmp", "wmv", "wp3", "wsve", "wvm", "wvx", "xej", "xesc", "xfl", "xlmv",
            "xmv", "xvid", "xyt", "yuv", "zm2", "zm3", "zmv", "zoom"
        )
    ),
    Compressed(
        setOf(
            "000", "001", "002", "004", "7z", "7z.001", "7z.002", "a00", "a01", "a02",
            "ace", "ain", "alz", "ana", "apex", "apz", "ar", "arc", "archiver", "arduboy", "arh", "arj", "ark",
            "asice", "ayt", "b1", "b6z", "bdoc", "bh", "bndl", "boo", "bundle", "bz", "bz2", "bzabw", "bzip2", "c00",
            "c01", "cb7", "cbr", "cbt", "cbz", "cdz", "cit", "comppkg.hauptwerk.rar", "conda", "cp9", "ctz",
            "cxarchive", "czip", "dar", "dd", "deb", "dgc", "dist", "dl_", "dz", "ecar", "ecs", "epi", "f", "f3z",
            "fdp", "fp8", "fzpz", "gca", "gmz", "gz", "gza", "gzip", "hbe", "hki", "hki1", "hki2", "hki3", "htmi",
            "iadproj", "ice", "ipk", "ish", "ita", "j", "jar.pack", "jex", "jgz", "jhh", "jsonlz4", "kextraction",
            "kgb", "ksp", "lbr", "lemon", "lha", "lpkg", "lqr", "lz", "lz4", "lzh", "lzm", "lzma", "memo", "mint",
            "mlproj", "movpkg", "mozlz4", "mpkg", "mzp", "nex", "npk", "nz", "oar", "odlgz", "opk", "osf", "oz",
            "p01", "p19", "p7z", "pa", "package", "pak", "paq7", "paq8", "paq8l", "paq8p", "par", "par2", "pax",
            "pbi", "pcv", "pea", "pet", "pf", "pima", "pit", "piz", "pkg", "pkg.tar.xz", "pkz", "pup", "pup", "puz",
            "pvmz", "pwa", "pxl", "q", "qda", "r0", "r00", "r01", "r03", "r1", "r2", "r21", "r30", "rar", "rev", "rk",
            "rnc", "rp9", "rpm", "rss", "rte", "rz", "s00", "s01", "s02", "s09", "s7z", "sar", "sbx", "sdc", "sdn",
            "sdocx", "sea", "sen", "sfg", "sfm", "sfs", "sfx", "shar", "shk", "sifz", "sipa", "sit", "sitx", "smpf",
            "snappy", "snb", "spt", "sqf", "sqx", "sqz", "srep", "stkdoodlz", "stproj", "sy_", "tar.bz2", "tar.gz",
            "tar.gz2", "tar.lz", "tar.lzma", "tar.xz", "taz", "tbz", "tbz2", "tcx", "tg", "tgs", "tgz", "tlzma",
            "tpsr", "trs", "tx_", "tzst", "ubz", "ufdr", "ufs.uzip", "uha", "vfs", "vip", "vms", "voca", "vpk",
            "vrpackage", "vwi", "wa", "war", "wastickers", "whl", "wick", "wlb", "wot", "xapk", "xcfbz2", "xcfgz",
            "xcfxz", "xez", "xfp", "xip", "xmcdz", "xopp", "xx", "xz", "y", "z", "z00", "z01", "z03", "z04", "zed",
            "zfsendtotarget", "zhelp", "zi", "zi_", "zip", "zipx", "zix", "zl", "zoo", "zpaq", "zpi", "zsplit",
            "zst", "zwi", "zz"
        )
    ),
    Link(setOf("htm", "html", "url", "webloc")),
    Locked(
        setOf(
            "aaa", "acid", "adame", "adobe", "aes", "aesir", "afp", "apkm", "atsofts",
            "aurora", "axx", "aze", "azf", "azs", "b2a", "bc5b", "bca", "bcup", "bfa", "bfe", "bhx", "bit",
            "blower", "bpk", "bpw", "bsk", "btoa", "bvd", "c9r", "cadq", "carote", "ccf", "cdoc", "cef", "cerber",
            "cerber2", "cgp", "chml", "clx", "cng", "codercrypt", "conti", "coot", "cpio", "cpt", "crypt", "crypt1",
            "crypted", "crypto", "cryptra", "ctbl", "cuid", "cuid2", "dc4", "dcd", "dcf", "dco", "ddoc", "ded",
            "devos", "dharma", "dim", "dime", "djvus", "dlc", "dm", "e4a", "ecd", "edfw", "edoc", "eegf", "eewt",
            "efdc", "efji", "efl", "efr", "efu", "eiur", "elbie", "emc", "enc", "encrypted", "enx", "eoc", "esf",
            "eslock", "exc", "extr", "fc", "fgsf", "filebolt", "film", "fonix", "fpenc", "fsm", "fun", "gdcb", "gero",
            "gfe", "givemenitro", "good", "gpg", "gxk", "gzquar", "hbx", "hex", "hid", "hid2", "hoop", "hqx",
            "htpasswd", "idea", "iwa", "jac", "jceks", "jcrypt", "jks", "jmc", "jmce", "jmck", "jmcp", "jmcr", "jmcx",
            "k3y", "kcxz", "kde", "keystore", "kifr", "kk", "kkk", "klq", "kode", "krab", "ks", "ksd", "kxx",
            "lastlogin", "lcn", "lilith", "lilocked", "litar", "locked", "locker", "locky", "lqqw", "lucy", "lvivt",
            "lxv", "maas", "mba", "mcq", "mcrp", "medusa", "meo", "merry", "mfs", "micro", "mim", "mime", "mjd",
            "mkf", "mme", "mnc", "mse", "mtzu", "nbes", "nc", "nitz", "nmo", "null", "nxl", "odin", "pack", "paradise",
            "pdc", "pdex", "pdy", "pfile", "pfo", "pfx", "pkey", "plp", "poop", "ppdf", "ppenc", "psw6", "ptrz",
            "purge", "pwv", "pxf", "pxx", "pyenc", "qewe", "qscx", "r2u", "r5a", "radman", "rap", "rcrypted", "rdi",
            "rem", "rensenware", "repp", "rrbb", "rsdf", "rumba", "ryk", "rzk", "rzx", "sa", "saf", "safe", "sage",
            "salma", "scb", "scb", "sdfi", "sdo", "sdoc", "sdtid", "seb", "sef", "sf", "sfi", "sgz", "shy", "sia",
            "signature", "sjpg", "sle", "sme", "snk", "spd", "spdf", "ssoi", "sspq", "stop", "stxt", "suf", "switch",
            "sxls", "sxml", "tar.md5", "tcvp", "thor", "u2k", "uea", "uiwix", "uu", "uud", "uue", "vdata", "viivo",
            "vlt", "voom", "vtym", "wallet", "wcry", "werd", "wiot", "wls", "wlu", "wncry", "wncryt", "wnry", "wolf",
            "wpe", "wrui", "wrypt", "xef", "xmdx", "xtbl", "xxe", "xxx", "yenc", "ykcol", "ynct", "zepto", "zip.enc",
            "zps", "zzzzz"
        )
    ),
    Executable(
        setOf(
            "0xe", "73k", "73p", "89k", "89z", "8ck", "a7r", "ac", "acc", "acr", "actc",
            "action", "actm", "afmacro", "afmacros", "ahk", "air", "apk", "app", "appimage", "applescript", "arscript",
            "asb", "atmx", "azw2", "ba_", "bat", "beam", "bin", "bms", "bns", "btm", "caction", "celx", "cfs", "cgi",
            "cheat", "cmd", "cof", "coffee", "com", "command", "csh", "cyw", "dek", "dld", "dxl", "e_e", "ear",
            "ebacmd", "ebm", "ebs", "ebs2", "ecf", "eham", "elf", "epk", "es", "esh", "ex4", "ex5", "ex_", "exe",
            "exe1", "exopc", "exz", "ezs", "ezt", "fap", "fas", "fba", "fky", "fpi", "frs", "fxp", "gadget", "gm9",
            "gpe", "gpu", "gs", "ham", "hms", "hpf", "hta", "icd", "iim", "ipa", "isu", "jar", "js", "jse", "jsf",
            "jsx", "kix", "ksh", "kx", "lo", "ls", "mac", "mamc", "mcr", "mel", "mem", "mgm", "mio", "mlappinstall",
            "mlx", "mm", "mpk", "mrc", "mrp", "ms", "msl", "mxe", "n", "ncl", "nexe", "ore", "osx", "otm", "out",
            "paf", "paf.exe", "pex", "phar", "pif", "plsc", "plx", "prc", "prg", "ps1", "pvd", "pwc", "pxo", "pyc",
            "pyo", "pyz", "qit", "qpx", "rbf", "rbx", "rfs", "rfu", "rgs", "rox", "rpg", "rpj", "run", "rxe", "s2a",
            "sapk", "sbs", "sca", "scar", "scpt", "scptd", "scr", "script", "sct", "seed", "server", "sh", "shb",
            "shortcut", "sk", "smm", "snap", "spr", "srec", "sts", "tcp", "tiapp", "tipa", "tms", "tpk", "u3p", "udf",
            "upx", "uvm", "uw8", "vbe", "vbs", "vbscript", "vexe", "vlx", "vpm", "vxp", "wcm", "widget", "widget",
            "wiz", "workflow", "wpk", "wpm", "ws", "wsf", "wsh", "x86", "x86_64", "xap", "xbap", "xbe", "xex", "xlm",
            "xqt", "xys", "ygh", "zl9"
        )
    ),
    Code(
        setOf(
            "blockx", "cdxml", "cham", "ckbx", "daconfig", "dfk", "dis", "fgl", "gmd", "k",
            "kps", "mlmodelc", "mode1v3", "mpy", "oml", "osc", "pbproj", "pineapple", "qx", "r", "rbg", "rbm", "rbvcp",
            "reds", "sb.branch", "storyboardc", "sublime-build", "vsixmanifest", "xojo_binary_window"
        )
    ),
    Font(
        setOf(
            "abf", "acfm", "afm", "amfm", "bdf", "bf", "bmfc", "cha",
            "compositefont", "dfont", "eot", "euf", "f3f", "fea", "ffil", "fnt", "fon", "fot", "gdr", "gf",
            "glif", "gxf", "jfproj", "lwfn", "mcf", "mf", "nftr", "odttf", "otf", "pcf", "pf2", "pfa", "pfb",
            "pfm", "pfr", "pft", "pk", "pmt", "sfp", "sft", "suit", "t65", "tfm", "ttc", "tte", "ttf", "txf",
            "ufo", "vfb", "vlw", "vnf", "woff", "woff2", "xfn", "xft", "ytf"
        )
    ),
    ThreeDImage(
        setOf(
            "3a3", "3c3", "3d", "3d2", "3d4", "3da", "3dc", "3df", "3dl", "3dm", "3dmf",
            "3dmk", "3don", "3dp", "3ds", "3dv", "3dw", "3dx", "3dxml", "3mf", "a2c", "a8s", "album", "amf", "an8",
            "anim", "animset", "animset_ingame", "anm", "aof", "aoi", "arexport", "arfx", "arm", "arpatch", "arproj",
            "arprojpkg", "asat", "atf", "atl", "atm", "b3d", "bbmodel", "bbscene", "bio", "bip", "bld", "blend", "blk",
            "br3", "br4", "br5", "br6", "br7", "brg", "brk", "bro", "bto", "bvh", "c3d", "c4d", "caf", "cal", "cal",
            "cas", "ccb", "ccp", "cfg", "cg", "cg3", "cga", "cgfx", "chr", "chrparams", "clara", "cm2", "cmdb", "cmf",
            "cmod", "cms", "cmz", "cpy", "crf", "crz", "csd", "csf", "csm", "cso", "d3d", "dae", "daz", "dbc", "dbl",
            "dbm", "dbs", "ddd", "ddp", "des", "dff", "dfs", "dmc", "dn", "drf", "ds", "dsa", "dsb", "dsd", "dse",
            "dsf", "dsi", "dso", "dsv", "duf", "dwf", "e57", "egg", "egm", "emcam", "exp", "f3d", "facefx",
            "facefx_ingame", "fbx", "fc2", "fcz", "fg", "fig", "flt", "fnc", "fp", "fp3", "fpe", "fpf", "fpj", "fry",
            "fsh", "fsq", "fuse", "fx", "fxa", "fxl", "fxm", "fxs", "fxt", "geo", "gh", "ghx", "glb", "glf", "glm",
            "glsl", "glslesf", "gltf", "gmf", "gmmod", "gmt", "grn", "grs", "hd2", "hdz", "hip", "hipnc", "hlsl",
            "hr2", "hrz", "hxn", "iavatar", "ifc", "iges", "igi", "igm", "igmesh", "igs", "ik", "irr", "irrmesh", "iv",
            "ive", "j3o", "jas", "jcd", "kfm", "kmc", "kmcobj", "ktz", "ldm", "llm", "lnd", "lp", "lps", "lt2", "ltz",
            "lwo", "lws", "lxf", "lxo", "m3", "m3d", "m3g", "ma", "makerbot", "mat", "max", "maxc", "mb", "mbx", "mc",
            "mc5", "mc6", "mcsg", "mcx-8", "mcz", "md5anim", "md5camera", "md5mesh", "mdd", "mdg", "mdl", "mdx", "meb",
            "mesh", "mesh", "mesh", "mgf", "mhm", "mix", "mmpp", "mnm", "mot", "mp", "mpj", "mqo", "mrml", "ms3d",
            "mtl", "mtx", "mtz", "mu", "mud", "mxm", "mxs", "n2", "n3d", "nff", "nif", "nm", "nsbta", "nxs", "obj",
            "obp", "obz", "oct", "off", "ogf", "ol", "p21", "p2z", "p3d", "p3l", "p3m", "p3r", "p4d", "p5d", "part",
            "pgal", "phy", "pigm", "pigs", "pl0", "pl1", "pl2", "ply", "pmd", "pmx", "pp2", "ppz", "prefab", "previz",
            "primitives", "primitives_processed", "prm", "prv", "psa", "psk", "pskx", "pz2", "pz3", "pzz", "qc", "rad",
            "ray", "rcs", "rds", "real", "reality", "rig", "s", "s3g", "s3o", "sbfres", "sbsar", "sc4model", "scw",
            "sdb", "sgn", "sh3d", "sh3f", "shapr", "shp", "si", "sis", "skl", "skp", "sm", "smc", "smd", "spline",
            "spv", "stc", "stel", "sto", "t3d", "tcn", "tddd", "tgo", "thing", "thl", "tilt", "tmd", "tme", "tmo",
            "tps", "trace", "tri", "ts1", "tvm", "u3d", "ums", "usd", "usdz", "v3d", "v3o", "v3v", "vac", "visual",
            "visual_processed", "vmd", "vmo", "vox", "vp", "vpd", "vrl", "vrm", "vroid", "vs", "vsh", "vso", "vtx",
            "vue", "vvd", "w3d", "wft", "wow", "wrl", "wrp", "wrz", "x", "x3d", "x3g", "xaf", "xmf", "xmm", "xof",
            "xpr", "xr", "xrf", "xsf", "xsi", "xv0", "yaodl", "ydl", "z3d", "zmbx", "zt", "zvf"
        )
    ),
    Photo(
        setOf(
            "3fr", "ari", "arw", "bay", "cr2", "cr3", "crw", "cs1", "cxi", "dcr", "dng",
            "dng", "eip", "erf", "fff", "gpr", "iiq", "j6i", "k25", "kc2", "kdc", "mdc", "mef", "mfw", "mos", "mrw",
            "nef", "nksc", "nrw", "orf", "pef", "raf", "raw", "rw2", "rwl", "rwz", "sr2", "srf", "srw", "x3f"
        )
    ),
    Movie(setOf("avi", "mkv", "mov")),
    Music(setOf("aac", "flac", "mp3")),
    Unknown(emptySet())
}

object FileTypeMapper {

    fun getFileType(filename: String): FileType = when (MimeTypeMap.getFileExtensionFromUrl(filename)) {
        in FileType.RasterImage.extensions -> FileType.RasterImage
        in FileType.VectorImage.extensions -> FileType.VectorImage
        in FileType.Text.extensions -> FileType.Text
        in FileType.Audio.extensions -> FileType.Audio
        in FileType.Video.extensions -> FileType.Video
        in FileType.Compressed.extensions -> FileType.Compressed
        in FileType.Link.extensions -> FileType.Link
        in FileType.Locked.extensions -> FileType.Locked
        in FileType.Executable.extensions -> FileType.Executable
        in FileType.Code.extensions -> FileType.Code
        in FileType.Font.extensions -> FileType.Font
        in FileType.ThreeDImage.extensions -> FileType.ThreeDImage
        in FileType.Photo.extensions -> FileType.Photo
        in FileType.Movie.extensions -> FileType.Movie
        in FileType.Music.extensions -> FileType.Music
        else -> FileType.Unknown
    }
}
