import React, { useState } from 'react';
import logo from './logo.svg';
//@ts-ignore
import * as deck from '@letele/playing-cards';
//import "react-poker/styles.css"
import './App.css';


// (possible exports: B1, B2, C10, C2, C3, C4, C5, C6, C7, C8, C9, Ca, Cj, Ck, Cq, D10, D2, D3, D4, D5, D6, D7, D8, D9, Da, Dj, Dk, Dq, H10, H2, H3, H4, H5, H6, H7, H8, H9, Ha, Hj, Hk, Hq, J1, J2, S10, S2, S3, S4, S5, S6, S7, S8, S9, Sa, Sj, Sk, Sq)

// [rank][suit]


const cards = ["Ks", "Qs", "2h", "6c", "Kc"].map(x => {
  let reversed = x.split('').reverse().join('');
  return reversed.charAt(0).toUpperCase() + reversed.slice(1).toLowerCase();
});

function App() {

  const [selectedFile, setSelectedFile] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [fileUrl, setFileUrl] = useState('');
  const [filesData, setFilesData] = useState<any[]>([]);

  // Handle file selection
  const handleFileChange = (e: any) => {
    setSelectedFile(e.target.files[0]);
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
          // Dynamically access the card from the 'deck' using the reversed key

          console.log(cardKey);
          const CardComponent = deck[cardKey];
          return (
            <div key={index} style={{ marginRight: '60px' }}>
              <CardComponent width={'100%'} height={'100%'} />
            </div>
          );
        })}
      </div>
      <input type="file" onChange={handleFileChange} />

      <button onClick={handleUpload} disabled={uploading}>
        {uploading ? 'Uploading...' : 'Upload'}
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
