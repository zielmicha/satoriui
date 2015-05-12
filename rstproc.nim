import packages/docutils/rstgen
import packages/docutils/rst

var gen: TRstGenerator
gen.initRstGenerator(outHtml, defaultConfig(), "filename", {})

let rstText = readAll(stdin).string

var hasToc: bool = false
let options: TRstParseOptions = {}
let rstTree = rstParse(text=rstText, filename="filename",
    line=0.int, column=0.int, hasToc=hasToc, options=options)

var generatedHTML = ""
gen.renderRstToOut(rstTree, generatedHTML)
echo generatedHTML
