const fs = require('fs');
const path = require('path');

const resultsDirectories = [
    { path: 'test-backup-volume-create/results', name: 'test-backup-volume-create/results' },
    { path: 'test-backup-volume-delete/results', name: 'test-backup-volume-delete/results' },
    { path: 'test-backup-volume-read/results', name: 'test-backup-volume-read/results' }
];

// Scan directories for HTML files
const htmlFiles = {};

resultsDirectories.forEach(dir => {
    const dirPath = path.join(__dirname, dir.path);
    
    if (fs.existsSync(dirPath)) {
        try {
            const files = fs.readdirSync(dirPath);
            const summaryFiles = files
                .filter(file => file.endsWith('-summary.html'))
                .sort();
            htmlFiles[dir.name] = summaryFiles;
            console.log(`Found ${summaryFiles.length} files in ${dir.path}`);
        } catch (error) {
            console.error(`Error reading ${dir.path}:`, error.message);
            htmlFiles[dir.name] = [];
        }
    } else {
        console.log(`Directory ${dir.path} does not exist`);
        htmlFiles[dir.name] = [];
    }
});

// Generate HTML content
const htmlContent = `<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Results</title>
</head>
<style>
    body {
        font-family: Arial, sans-serif;
        margin: 20px;
    }
    select, button {
        margin: 10px 0;
        padding: 5px;
    }
</style>
<body>
    <h1>Stress Test Results</h1>

    <div style="display: flex; justify-content: center; align-items: center; flex-direction: column;">
    <label for="reportSelect">Select a Report Type:</label>
    <select id="reportSelect" onchange="loadHtmlFiles()">
        <option value="">Select a report</option>
        <option value="test-backup-volume-create/results">Backup Volume Create Summary</option>
        <option value="test-backup-volume-delete/results">Backup Volume Delete Summary</option>
        <option value="test-backup-volume-read/results">Backup Volume Read Summary</option>
    </select>

    <label for="htmlFileSelect">Select an HTML Report:</label>
    <select id="htmlFileSelect" disabled>
        <option value="">Select an HTML file</option>
    </select>
    
    <button onclick="showReport()">Show Report</button>
    </div>
    <script>
        // Auto-generated file list from scanning directories
        const htmlFiles = ${JSON.stringify(htmlFiles, null, 8)};

        function loadHtmlFiles() {
            const reportSelect = document.getElementById('reportSelect');
            const htmlFileSelect = document.getElementById('htmlFileSelect');
            const selectedReport = reportSelect.value;

            // Reset the HTML file dropdown
            htmlFileSelect.innerHTML = '<option value="">Select an HTML file</option>';
            htmlFileSelect.disabled = true;

            if (selectedReport && htmlFiles[selectedReport]) {
                const files = htmlFiles[selectedReport];
                if (files.length > 0) {
                    files.forEach(file => {
                        const option = document.createElement('option');
                        option.value = selectedReport + '/' + file;
                        option.textContent = file.replace('-summary.html', '').replace(/-/g, ' ');
                        htmlFileSelect.appendChild(option);
                    });
                    htmlFileSelect.disabled = false;
                } else {
                    const option = document.createElement('option');
                    option.textContent = 'No HTML files available';
                    htmlFileSelect.appendChild(option);
                }
            }
        }

        function showReport() {
            const htmlFileSelect = document.getElementById('htmlFileSelect');
            const selectedFile = htmlFileSelect.value;

            if (selectedFile) {
                window.location.href = selectedFile;
            } else {
                alert('Please select a report type and an HTML file');
            }
        }
    </script>
</body>
</html>`;

// Write the HTML file
const outputPath = path.join(__dirname, 'Results.html');
fs.writeFileSync(outputPath, htmlContent);
console.log(`\nGenerated ${outputPath}`);
