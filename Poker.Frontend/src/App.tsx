import React, { useState } from 'react';
import logo from './logo.svg';
//@ts-ignore
import * as deck from '@letele/playing-cards';

import './App.css';


const cards = ["Ks", "Qs", "2h", "6c", "Kc"].map(x => {

  if (x[1] == 's') {
    return x[0] + 'a'
  }

  if (x[1] == 'h') {
    return x[0] + 'n'
  }

  if (x[1] == 'c') {
    return x[0] + 'f'
  }

  if (x[1] == 'd') {
    return x[0] + 'e'
  }

});

function App() {

  console.log(cards);

  const [selectedFile, setSelectedFile] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [fileUrl, setFileUrl] = useState('');
  const [filesData, setFilesData] = useState<any[]>([]);

  // Handle file selection
  const handleFileChange = (e: any) => {
    setSelectedFile(e.target.files[0]);
  };


  const downloadFile = async () => {


  }


  const handleDeserialization = async () => {
    //12.05.2025 -> pass folder instead file (based on fileName)
    const url = `https://us-central1-cognispace.cloudfunctions.net/mkr-file-processor?file=test (2).mkr/0_test (2).mkr`;
  
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
      // optionally: setFilesData(text);
    } catch (error) {
      console.error('Error during deserialization:', error);
    }
  };
  

  // Handle file upload
  const handleUpload = async () => {
    if (selectedFile) {
      setUploading(true);

      const formData = new FormData();
      formData.append('file', selectedFile);

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
          alert('File uploaded successfully!');
        } else {
          alert('Error uploading file.');
        }
      } catch (error) {
        console.error('Error uploading file:', error);
        alert('Error uploading file.');
      } finally {
        setUploading(false);
      }
    } else {
      alert('Please select a file to upload');
    }
  };

  return (
    <>
      <div style={{ display: 'flex', padding: 80 }} className="App">
        {cards.map((cardKey, index) => {
          console.log(cardKey); // e.g., "2e"
          return (
            <div key={index} style={{ marginRight: '20px' }}>
              <img
                src={`/deck/${cardKey?.toLowerCase()}.png`}
                alt={cardKey}
                style={{ width: '40px', height: '60px' }}
              />
            </div>
          );
        })}
      </div>
      <input type="file" onChange={handleFileChange} />

      <button onClick={handleUpload} disabled={uploading}>
        {uploading ? 'Uploading...' : 'Upload'}
      </button>

      <button onClick={handleDeserialization}>
        Deserialize
      </button>

      <button onClick={downloadFile}>
        Download
      </button>

      {fileUrl && (
        <div>
          <h2>Download Link:</h2>
          <a href={fileUrl} target="_blank" rel="noopener noreferrer">
            {fileUrl}
          </a>
        </div>
      )}
      <ul>
        {filesData.length > 0 && (
          filesData.map((file, index) => (
            <li key={index}>
              {file.file_path} - {file.size} bytes
            </li>
          )))}
      </ul>
    </>
  );
}

export default App;
