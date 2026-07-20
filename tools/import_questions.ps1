param(
  [Parameter(Mandatory=$true)][string]$Workbook,
  [Parameter(Mandatory=$true)][string]$Output
)

Add-Type -AssemblyName System.IO.Compression.FileSystem

function Read-Workbook([string]$Path) {
  $zip = [IO.Compression.ZipFile]::OpenRead((Resolve-Path -LiteralPath $Path))
  try {
    $shared = [Collections.Generic.List[string]]::new()
    $entry = $zip.GetEntry('xl/sharedStrings.xml')
    if ($entry) {
      $reader = [IO.StreamReader]::new($entry.Open())
      try { [xml]$xml = $reader.ReadToEnd() } finally { $reader.Dispose() }
      $ns = [Xml.XmlNamespaceManager]::new($xml.NameTable); $ns.AddNamespace('x','http://schemas.openxmlformats.org/spreadsheetml/2006/main')
      foreach ($item in $xml.SelectNodes('//x:si',$ns)) { [void]$shared.Add((($item.SelectNodes('.//x:t',$ns) | ForEach-Object {$_.InnerText}) -join '')) }
    }
    $entry=$zip.GetEntry('xl/workbook.xml'); $reader=[IO.StreamReader]::new($entry.Open()); try {[xml]$book=$reader.ReadToEnd()}finally{$reader.Dispose()}
    $bookNs=[Xml.XmlNamespaceManager]::new($book.NameTable);$bookNs.AddNamespace('x','http://schemas.openxmlformats.org/spreadsheetml/2006/main')
    $entry=$zip.GetEntry('xl/_rels/workbook.xml.rels');$reader=[IO.StreamReader]::new($entry.Open());try{[xml]$rels=$reader.ReadToEnd()}finally{$reader.Dispose()}
    $relNs=[Xml.XmlNamespaceManager]::new($rels.NameTable);$relNs.AddNamespace('r','http://schemas.openxmlformats.org/package/2006/relationships');$targets=@{}
    foreach($rel in $rels.SelectNodes('//r:Relationship',$relNs)){$targets[$rel.Id]=$rel.Target}
    $result=@{}
    foreach($sheet in $book.SelectNodes('//x:sheets/x:sheet',$bookNs)) {
      if($sheet.sheetId -notin @('1','2')){continue}
      $id=$sheet.GetAttribute('id','http://schemas.openxmlformats.org/officeDocument/2006/relationships');$target=$targets[$id];$pathInZip=if($target.StartsWith('/')){$target.TrimStart('/')}else{'xl/'+$target}
      $entry=$zip.GetEntry($pathInZip);$reader=[IO.StreamReader]::new($entry.Open());try{[xml]$page=$reader.ReadToEnd()}finally{$reader.Dispose()}
      $pageNs=[Xml.XmlNamespaceManager]::new($page.NameTable);$pageNs.AddNamespace('x','http://schemas.openxmlformats.org/spreadsheetml/2006/main');$questions=[Collections.Generic.List[object]]::new()
      foreach($row in $page.GetElementsByTagName('row')) {
        $cells=@{}
        foreach($cell in @($row.ChildNodes | Where-Object {$_.LocalName -eq 'c'})) {
          $valueNode=@($cell.GetElementsByTagName('v')) | Select-Object -First 1
          $cellType=$cell.GetAttribute('t')
          $value=if($cellType -eq 's' -and $valueNode){$shared[[int]$valueNode.InnerText]}elseif($cellType -eq 'inlineStr'){(($cell.GetElementsByTagName('t')|ForEach-Object{$_.InnerText})-join '')}elseif($valueNode){$valueNode.InnerText}else{''}
          if($value -ne ''){$cells[([regex]::Match($cell.GetAttribute('r'),'^[A-Z]+')).Value]=$value}
        }
        if((($cells['A']) -match '^\d+$') -and $cells.ContainsKey('B')) {
          [void]$questions.Add([ordered]@{id=[int]$cells['A'];stem=$cells['B'];options=@($cells['C'],$cells['D'],$cells['E'],$cells['F']);answer=$cells['G']})
        }
      }
      $key = if($sheet.sheetId -eq '1'){'paper1'}else{'paper3'}
      $result[$key]=$questions
    }
    return $result
  } finally { $zip.Dispose() }
}

$data=Read-Workbook $Workbook
$paper1=$data['paper1']; $paper3=$data['paper3']
if($paper1.Count -ne 2525 -or $paper3.Count -ne 1999){throw "Unexpected question count: P1=$($paper1.Count), P3=$($paper3.Count)"}
[IO.Directory]::CreateDirectory((Split-Path -Parent $Output)) | Out-Null
$json=$data | ConvertTo-Json -Depth 5 -Compress
[IO.File]::WriteAllText((Resolve-Path -LiteralPath (Split-Path -Parent $Output)).Path + '\' + (Split-Path -Leaf $Output),$json,[Text.UTF8Encoding]::new($false))
Write-Host "Generated $Output with $($paper1.Count) Paper I and $($paper3.Count) Paper III questions."
