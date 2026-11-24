@startuml
autonumber
actor "Ticket Validator" as Val
participant "ScannerScreen (UI)" as UI
participant "Camera / QR Decoder" as Cam
participant "ValidationController" as Ctrl
participant "Backend API" as API
database "Database" as DB

Val -> UI : Open Scanner screen
UI -> Cam : Request camera preview
Cam --> Val : Display live viewfinder

Val -> Cam : Point camera at Ticket QR Code
Cam -> Ctrl : onCodeDetected(ticketCode)
Ctrl -> UI : Show loading / processing indicator
Ctrl -> API : POST /tickets/validate (AuthToken, ticketCode)

API -> API : Verify VALIDATOR role permission
API -> DB : Query ticket by ticketCode

alt Ticket Not Found (e.g., UNKNOWN-TICKET-999)
DB --> API : Null
API --> Ctrl : 404 Not Found ("Invalid / Unknown Ticket")
Ctrl --> UI : Display Red Error Banner (Unknown Code)
else Ticket Already Used (Status == USED)
DB --> API : Ticket Record (status: USED, usedAt)
API --> Ctrl : 409 Conflict ("Ticket Already Used")
Ctrl --> UI : Display Warning/Error Banner (Already Scanned)
else Ticket Valid & Unused (Status == VALID)
DB --> API : Ticket Record (status: VALID)
API -> DB : Update status = USED, set usedAt = now()
DB --> API : Update confirmed
API --> Ctrl : 200 OK ("Valid Ticket")
Ctrl --> UI : Display Green Success Banner (Entry Permitted)
end
@enduml