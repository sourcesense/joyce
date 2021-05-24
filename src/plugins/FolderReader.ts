const fs = require("fs");
const fsPromises = require("fs").promises;
//joining path of directory
//const directoryPath = path.join(__dirname, "Documents");
//passsing directoryPath and callback function
export function readerFolder(directoryPath: string) {
  return fs
    .readdirSync(directoryPath)
    .map((file: string) => `${directoryPath}/${file}`);
}

export function readerFolderPromise(directoryPath) {
  return fsPromises.readdir(directoryPath).then((list) => {
    return list.map((file: string) => `${directoryPath}/${file}`);
  });
}
