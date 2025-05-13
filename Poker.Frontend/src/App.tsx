import React, { useState } from 'react';
import './App.css';



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
  const [fileUrl, setFileUrl] = useState('');
  const [filesData, setFilesData] = useState<any[]>([]);

  const [fileName, setFileName] = useState<string>('test.mkr');

  const [cards, setCards] = useState<any[]>([]); // Initialize cards state as an empty array


  const [files, setFiles] = useState([]);

  // Handle file selection
  const handleFileChange = (e: any) => {
    setSelectedFile(e.target.files[0]);
  };


  const downloadFiles = async () => {
    const functionUrl = "https://us-central1-cognispace.cloudfunctions.net/downloadFilesFunction"; // Replace with your function URL

    const folderName = fileName + '/deserialized/'

    console.log(folderName);

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
    } catch (err) {
      console.error("Failed to fetch files:", err);
    }
  };



  const handleDeserialization = async () => {
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
      // optionally: setFilesData(text);
    } catch (error) {
      console.error('Error during deserialization:', error);
      setDeserializing(false);
    }
  };


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
          handleDeserialization()
          handleDeserialization()
          alert('File uploaded successfully!');
        } else {
          alert('Error uploading file.');
        }
      } catch (error) {
        console.error('Error uploading file:', error);
        alert('Error uploading file.');
      } finally {
        setUploading(false);
        handleDeserialization();
      }
    } else {
      alert('Please select a file to upload');
    }
  };

  return (
    <>
      <div style={{ display: 'flex', padding: 80 }} className="App">

        {!cards || cards.length === 0 && (
          <div style={{ marginRight: '20px' }}>
            No Cards
          </div>
        )}
        {cards.map((cardKey, index) => {
          console.log(cardKey); // e.g., "2e"
          return (
            <div key={index} style={{ marginRight: '20px' }}>
              <img
                src={`${process.env.PUBLIC_URL}/deck/${cardKey}.png`}
                alt={cardKey}
                style={{ width: '40px', height: '60px', }}
              />
            </div>
          );
        })}
      </div>
      <input type="file" onChange={handleFileChange} />

      <button onClick={handleUpload} disabled={uploading}>
        {uploading ? 'Uploading...' : 'Upload'}
      </button>

      {fileName && (
        <>
          <p>File Name: {fileName}</p>

        </>
      )}

      {deserializing && (
        <div>
          <p>Deserializing... GCP bucket in folder: {fileName}</p>
        </div>
      )}


      <button onClick={handleDeserialization}>
        Deserialize
      </button>

      <button onClick={() => downloadFiles()}>
        Download Files
      </button>

      {/* <button onClick={downloadFile}>
        Download
      </button> */}

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
          <h2>Files:</h2>
          <ul></ul>
          {files.map((file: any, idx: any) => (
            <div key={idx}>
              {file.text ? (
                <pre style={{ backgroundColor: "#f0f0f0", padding: "10px" }}>
                  {file.text}
                </pre>
              ) : (
                <a href={file.url} download={file.name}>Download</a>
              )}
            </div>
          ))}
        </div>
      )}
      <hr />
    </>
  );
}

export default App;
