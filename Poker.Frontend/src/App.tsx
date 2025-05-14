import React, { useEffect, useState } from 'react';
import './App.css';
import { ClipLoader } from "react-spinners";


const buttonStyle = {
  marginRight: '10px',
  padding: '8px 16px',
  backgroundColor: '#007bff',
  color: 'white',
  border: 'none',
  borderRadius: '4px',
  cursor: 'pointer',
};


const transformCards = (cardsArray: any) => {
  return cardsArray.map((x: string[]) => {
    if (x[1] === 's') {
      return x[0] + 'a';  // 's' -> 'a'
    }
    if (x[1] === 'h') {
      return x[0] + 'n';  // 'h' -> 'n'
    }
    if (x[1] === 'c') {
      return x[0] + 'f';  // 'c' -> 'f'
    }
    if (x[1] === 'd') {
      return x[0] + 'e';  // 'd' -> 'e'
    }
    return x;  // return the card as is if none of the conditions match
  });
}

const extractCards = (inputString: string) => {
  const regex = /\[([^\]]+)\]/; // Matches the content inside square brackets []
  const match = inputString.match(regex);
  if (match && match[1]) {
    return match[1].split(',').map(card => card.trim()); // Split the cards and remove extra spaces
  }
  return [];
};

function App() {

  const [selectedFile, setSelectedFile] = useState(null);
  const [uploading, setUploading] = useState(false);


  const [deserializing, setDeserializing] = useState(false);
  const [downloading, setDownloading] = useState(false);


  const [fileUrl, setFileUrl] = useState('');
  const [filesData, setFilesData] = useState<any[]>([]);

  const [fileName, setFileName] = useState<string>('');

  const [cards, setCards] = useState<any[]>([]); // Initialize cards state as an empty array


  const [files, setFiles] = useState([]);
  const [loading, setLoading] = useState(false);

  // Handle file selection
  const handleFileChange = (e: any) => {
    setSelectedFile(e.target.files[0]);
  };


  const downloadFiles = async (initialFileName?: any) => {

    setLoading(true);
    const functionUrl = "https://us-central1-cognispace.cloudfunctions.net/downloadFilesFunction"; // Replace with your function URL

    let folderName;
    console.log(initialFileName);

    if (initialFileName) {
      folderName = initialFileName + '/deserialized/';
    }
    else {
      setDownloading(true);
      setTimeout(() => {
      }, 5000);
      folderName = fileName + '/deserialized/';
    }

    try {
      const response = await fetch(functionUrl, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ folderName }),
      });

      if (!response.ok) {
        const error = await response.json();
        console.error("Error:", error.message);
        return;
      }

      const data = await response.json();
      const files = data.files || [];

      // Convert base64 to Blob URLs or raw text if needed
      const processedFiles = files.map((file: any) => {
        const byteCharacters = atob(file.content_base64);
        const byteNumbers = Array.from(byteCharacters).map(c => c.charCodeAt(0));
        const byteArray = new Uint8Array(byteNumbers);
        const blob = new Blob([byteArray], { type: file.content_type });

        if (byteCharacters.includes("6_") && !byteCharacters.includes("16_")) {
          const extractedCards = extractCards(byteCharacters);

          // Transform the cards
          const transformedCards = transformCards(extractedCards);

          // Update the state with the transformed cards
          setCards(transformedCards);
        }


        return {
          name: file.file_name,
          contentType: file.content_type,
          size: file.size_bytes,
          blob,
          url: URL.createObjectURL(blob), // if you want to preview/download later
          text: file.content_type.startsWith("text") ? byteCharacters : null,
        };
      });

      setFiles(processedFiles);
      setLoading(false);
    } catch (err) {
      console.error("Failed to fetch files:", err);
    } finally {
      setDownloading(false);
    }
  };



  const handleDeserialization = async (fileName: string) => {
    //12.05.2025 -> pass folder instead file (based on fileName)
    //✅ Fix: Ensure folderName ends with /
    const url = `https://us-central1-cognispace.cloudfunctions.net/mkr-file-processor?file=${fileName}/`;

    setDeserializing(true);
    try {
      const response = await fetch(url, {
        method: 'GET', // or 'POST' if needed, but must match what the function supports
        headers: {
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        throw new Error('Network response was not ok');
      }

      const text = await response.text(); // response is plain text
      console.log('Deserialized data:', text);
      setDeserializing(false);
      fetchFolders();
      setTimeout(() => {
      }, 3000);
      downloadFiles(fileName);
      // optionally: setFilesData(text);
    } catch (error) {
      console.error('Error during deserialization:', error);
      setTimeout(() => {
      }, 3000);
      downloadFiles(fileName);
      setDeserializing(false);
      fetchFolders();
    }
  };


  const [folders, setFolders] = useState([]);

  async function fetchFolders() {
    const response = await fetch(
      "https://storage.googleapis.com/storage/v1/b/auxilia-poker/o?delimiter=/"
    );
    const data = await response.json();
    console.log(data);
    const folders = data.prefixes.filter((x: any) => x != 'deck/' && x != 'static/' && x != 'tools/'); // Folders will be like "images/", "assets/"
    console.log(folders);
    if (data.prefixes) {
      setFolders(folders)
    }
  }

  useEffect(() => {
    fetchFolders()
  }, []);

  const [selectedFolder, setSelectedFolder] = useState('');


  function loadFolder(folder: React.SetStateAction<string>) {
    setSelectedFolder(folder);
    //@ts-ignore
    setFileName(folder.replace('/', ''));
    //@ts-ignore
    downloadFiles(folder.replace('/', ''));

    setCards([]);
    // Optional: trigger file list download logic for this folder
  }

  // Handle file upload
  const handleUpload = async () => {
    if (selectedFile) {
      setUploading(true);


      const formData = new FormData();
      formData.append('file', selectedFile);

      console.log(selectedFile)
      //@ts-ignore
      setFileName(selectedFile.name);

      try {
        // Send the file to the serverless function endpoint
        const response = await fetch('https://us-central1-cognispace.cloudfunctions.net/uploadFileFunction', {
          method: 'POST',
          headers: {
            'Accept': 'application/json',
          },
          body: formData,
        });

        const data = await response.json();

        setFilesData(data);

        if (data.fileUrl) {
          setFileUrl(data.fileUrl); // Get the file URL after uploading
        }
      } catch (error) {
        console.error('Error uploading file:', error);
        alert('Error uploading file.');
      } finally {
        setUploading(false);
        setTimeout(() => {
        }, 3000);
        //@ts-ignore
        handleDeserialization(selectedFile?.name);
      }
    } else {
      alert('Please select a file to upload');
    }
  };

  const [expandedIndexes, setExpandedIndexes] = useState<boolean[]>([]);

  const toggleExpand = (idx: number) => {
    setExpandedIndexes((prev) => {
      const updated = [...prev];
      updated[idx] = !updated[idx];
      return updated;
    });
  };

  return (
    <>
      <div style={{ display: 'flex' }}>
        {/* Left Panel - Folder List */}
        <div
          style={{
            minWidth: '200px',
            maxWidth: '200px',
            backgroundColor: '#f4f4f4',
            padding: '20px',
            borderRight: '1px solid #ccc',
            minHeight: '100vh',
          }}
        >
          <ul style={{ listStyle: 'none', padding: 0 }}>
            {folders.map((folder: any, index) => (
              <li key={index}>
                <button
                  onClick={() => loadFolder(folder)}
                  style={{
                    width: '100%',
                    margin: '5px 0',
                    padding: '8px',
                    backgroundColor: selectedFolder === folder ? '#007bff' : '#e0e0e0',
                    color: selectedFolder === folder ? 'white' : 'black',
                    border: 'none',
                    borderRadius: '4px',
                    textAlign: 'left',
                    cursor: 'pointer',
                  }}
                >
                  {folder.replace('/', '')}
                </button>
              </li>
            ))}
          </ul>
        </div>

        {loading ?
          <div
            style={{
              display: 'flex',
              justifyContent: 'center',
              alignItems: 'center',
              height: '100vh',
              width: '100vw',
            }}
          >
            <ClipLoader />
          </div>

          :
          <div style={{ flex: 1, padding: '40px' }}>
            <div style={{ display: 'flex' }} className="App">
              {!cards || cards.length === 0 ? (
                <div style={{ display: 'flex' }}>
                  {[...Array(5)].map((_, idx) => (
                    <div key={idx} style={{ width: '40px', height: '60px', backgroundColor: 'gray', marginRight: '20px' }} />
                  ))}
                </div>
              ) : (
                cards.map((cardKey, index) => (
                  <div key={index} style={{ display: 'flex', marginRight: '20px' }}>
                    <img
                      src={`https://storage.googleapis.com/auxilia-poker/deck/${cardKey}.png`}
                      alt={cardKey}
                      style={{ width: '40px', height: '60px' }}
                    />
                  </div>
                ))
              )}
            </div>

            {/* File input and buttons */}
            <input type="file" onChange={handleFileChange} style={{ marginTop: '20px' }} />
            <div style={{ marginTop: '10px' }}>
              <button onClick={handleUpload} disabled={uploading} style={buttonStyle}>
                {uploading ? 'Uploading...' : 'Start Processing'}
              </button>

              <button onClick={downloadFiles} style={buttonStyle}>
                Download Files
              </button>
            </div>

            {/* {fileName && <p style={{ marginTop: '10px' }}>File Name: {fileName}</p>} */}
            {deserializing && <p>Deserializing... GCP bucket in folder: {fileName}</p>}
            {downloading && <p>Downloading...</p>}
            {fileUrl && (
              <div>
                <h2>Download Link:</h2>
                <a href={fileUrl} target="_blank" rel="noopener noreferrer">
                  {fileUrl}
                </a>
              </div>
            )}

            {files.length > 0 && (
              <div>
                <h2>Data:</h2>
                {files.sort((a: any, b: any) => b.size - a.size).map((file: any, idx) => (
                  <div style={{ whiteSpace: 'pre-wrap', width: '800px', marginBottom: '10px' }} key={idx}>
                    {file.text ? (
                      <div>
                        {file.name.split('_')[0]} -
                        {file.size} Bytes
                        <button
                          onClick={() => toggleExpand(idx)}
                          style={{
                            marginBottom: '8px',
                            padding: '5px 10px',
                            cursor: 'pointer',
                            backgroundColor: '#007bff',
                            color: 'white',
                            border: 'none',
                            borderRadius: '4px'
                          }}
                        >
                          {expandedIndexes[idx] ? '-' : '+'}
                        </button>
                        {expandedIndexes[idx] && (
                          <pre
                            style={{
                              whiteSpace: 'pre-wrap',
                              width: '800px',
                              backgroundColor: '#f0f0f0',
                              padding: '10px',
                              border: '1px solid #ccc'
                            }}
                          >
                            {file.text}
                          </pre>
                        )}
                      </div>
                    ) : (
                      <a href={file.url} download={file.name}>
                        {file.name}
                      </a>
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>
        }
      </div>
    </>
  );
}

export default App;
