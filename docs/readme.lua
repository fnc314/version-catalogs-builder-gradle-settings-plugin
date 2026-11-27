function Pandoc(doc)
  local first_block = doc.blocks[1]
  if first_block and first_block.t == "Header" and first_block.level == 1 then
    doc.meta.title = pandoc.utils.stringify(first_block.content)
  end
  return doc
end

function Link(link)
  if link.target:match("^samples/") then
    link.target = "https://github.com/fnc314/version-catalogs-builder-gradle-settings-plugin/tree/main/" .. link.target
  end
  return link
end