<<<<<<< HEAD
# IMPORTS
from __future__ import print_function
from googleapiclient.discovery import build
from apiclient import errors
from httplib2 import Http
from email.mime.text import MIMEText
import base64
from google.oauth2 import service_account
import google.auth


  
# Email variables. Modify this!
#EMAIL_FROM = 'villaget3ch@gmail.com'

EMAIL_FROM = 'admin-420@services-1729.iam.gserviceaccount.com'
#EMAIL_FROM = 'servitor@services-1729.iam.gserviceaccount.com'

EMAIL_TO = 'anubhav.balodhi@gmail.com'
EMAIL_SUBJECT = 'Hello, from Zippe!'
EMAIL_CONTENT = 'Hi, this is a test\nZippe\nhttps://zippe.in'


# FUNCTIONS

def create_message(sender, to, subject, message_text):
  """Create a message for an email.

  Args:
    sender: Email address of the sender.
    to: Email address of the receiver.
    subject: The subject of the email message.
    message_text: The text of the email message.

  Returns:
    An object containing a base64url encoded email object.
  """
  message = MIMEText(message_text)
  message['to'] = to
  message['from'] = sender
  message['subject'] = subject
  b64_bytes = base64.urlsafe_b64encode(message.as_bytes())
  b64_string = b64_bytes.decode()
  ret = {'raw': b64_string}
  return  ret #{'raw': base64.urlsafe_b64encode(message.as_string())}

def send_message(service, user_id, message):
  """Send an email message.

  Args:
    service: Authorized Gmail API service instance.
    user_id: User's email address. The special value "me"
    can be used to indicate the authenticated user.
    message: Message to be sent.

  Returns:
    Sent Message.
  """
  try:
    message = (service.users().messages().send(userId=user_id, body=message)
               .execute())
    print('Message Id: %s' % message['id'])
    return message
  except errors.HttpError as error:
    print('An error occurred: %s' % error)


def service_account_login():
  """
    login to the service account created with the json file
  """
  SCOPES = ['https://www.googleapis.com/auth/gmail.send']
  #SERVICE_ACCOUNT_FILE = 'services-1729-23ccdc427424.json'
  SERVICE_ACCOUNT_FILE = '/home/Anubhav Pandey/work/zp_exp/server/google-cloud/services-1729-d85e5765d66c.json'
  

  credentials = service_account.Credentials.from_service_account_file(
          SERVICE_ACCOUNT_FILE, scopes=SCOPES)
  delegated_credentials = credentials.with_subject(EMAIL_FROM)
  service = build('gmail', 'v1', credentials=delegated_credentials)
  return service
  
  

service = service_account_login()
# Call the Gmail API
message = create_message(EMAIL_FROM, EMAIL_TO, EMAIL_SUBJECT, EMAIL_CONTENT)
sent = send_message(service,'me', message)
=======
# IMPORTS
from __future__ import print_function
from googleapiclient.discovery import build
from apiclient import errors
from httplib2 import Http
from email.mime.text import MIMEText
import base64
from google.oauth2 import service_account
import google.auth


  
# Email variables. Modify this!
#EMAIL_FROM = 'villaget3ch@gmail.com'

EMAIL_FROM = 'admin-420@services-1729.iam.gserviceaccount.com'
#EMAIL_FROM = 'servitor@services-1729.iam.gserviceaccount.com'

EMAIL_TO = 'anubhav.balodhi@gmail.com'
EMAIL_SUBJECT = 'Hello, from Zippe!'
EMAIL_CONTENT = 'Hi, this is a test\nZippe\nhttps://zippe.in'


# FUNCTIONS

def create_message(sender, to, subject, message_text):
  """Create a message for an email.

  Args:
    sender: Email address of the sender.
    to: Email address of the receiver.
    subject: The subject of the email message.
    message_text: The text of the email message.

  Returns:
    An object containing a base64url encoded email object.
  """
  message = MIMEText(message_text)
  message['to'] = to
  message['from'] = sender
  message['subject'] = subject
  b64_bytes = base64.urlsafe_b64encode(message.as_bytes())
  b64_string = b64_bytes.decode()
  ret = {'raw': b64_string}
  return  ret #{'raw': base64.urlsafe_b64encode(message.as_string())}

def send_message(service, user_id, message):
  """Send an email message.

  Args:
    service: Authorized Gmail API service instance.
    user_id: User's email address. The special value "me"
    can be used to indicate the authenticated user.
    message: Message to be sent.

  Returns:
    Sent Message.
  """
  try:
    message = (service.users().messages().send(userId=user_id, body=message)
               .execute())
    print('Message Id: %s' % message['id'])
    return message
  except errors.HttpError as error:
    print('An error occurred: %s' % error)


def service_account_login():
  """
    login to the service account created with the json file
  """
  SCOPES = ['https://www.googleapis.com/auth/gmail.send']
  #SERVICE_ACCOUNT_FILE = 'services-1729-23ccdc427424.json'
  SERVICE_ACCOUNT_FILE = '/home/Anubhav Pandey/work/zp_exp/server/google-cloud/services-1729-d85e5765d66c.json'
  

  credentials = service_account.Credentials.from_service_account_file(
          SERVICE_ACCOUNT_FILE, scopes=SCOPES)
  delegated_credentials = credentials.with_subject(EMAIL_FROM)
  service = build('gmail', 'v1', credentials=delegated_credentials)
  return service
  
  

service = service_account_login()
# Call the Gmail API
message = create_message(EMAIL_FROM, EMAIL_TO, EMAIL_SUBJECT, EMAIL_CONTENT)
sent = send_message(service,'me', message)
>>>>>>> dev
