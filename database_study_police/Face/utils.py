from PIL import Image
import face_recognition
import os
import numpy

def crop_save_face(self, image_name, user):
    encoding = {}

    test_image = face_recognition.load_image_file('./media/TestFaces/' + image_name)
    locations = face_recognition.face_locations(test_image)
    encoding = face_recognition.face_encodings(test_image)

    if len(encoding) == 0:
        return False

    if locations is None or len(locations)==0:
        return False

    face_loaction = face_recognition.face_locations(test_image)[0]

    if face_loaction is None:
        return False

    top, right, bottom, left = face_loaction
    cropped_face = test_image[top:bottom, left:right]

    pil_image = Image.fromarray(cropped_face)
    pil_image.save('./media/Faces/' + str(user) + '.jpg', 'JPEG')

    image = face_recognition.load_image_file('./media/Faces/' + image_name)
    encoding = face_recognition.face_encodings(image)
    
    if len(encoding) == 0:
        path = './media/Faces' 
        filename = str(user) + '.jpg'
        os.remove(os.path.join(path, filename))
        return False
    

    return True






def get_encoded_faces():
    """
    looks through the faces folder and encodes all
    the faces

    :return: dict of (name, image encoded)
    """
    encoded = {}

    for dirpath, dnames, fnames in os.walk("./media/Faces"):
        for f in fnames:
            if f.endswith(".jpg") or f.endswith(".png"):
                face = face_recognition.load_image_file("./media/Faces/" + f)
                encoding = face_recognition.face_encodings(face)
                if len(encoding) != 0:
                    encoded[f.split(".")[0]] = encoding[0]

    return encoded



def unknown_image_encoded(image_name):
    """
    encode a face given the file name
    """
    face = face_recognition.load_image_file("./media/TestFaces/" + image_name)
    encoding = face_recognition.face_encodings(face)[0]

    return encoding



def match_face(image_name, user):
    """
    will find all of the faces in a given image and label
    them if it knows what they are

    :param im: str of file path
    :return: list of face names
    """

    is_found = False

    faces = get_encoded_faces()

    if faces is None:
        return is_found

    faces_encoded = list(faces.values())
    known_face_names = list(faces.keys())

    if faces_encoded is None or len(faces_encoded)==0:
        return is_found

    test_image = face_recognition.load_image_file("./media/TestFaces/" + image_name)
 
    face_locations = face_recognition.face_locations(test_image)
    unknown_face_encodings = face_recognition.face_encodings(test_image, face_locations)

    face_names = []
    for face_encoding in unknown_face_encodings:
        # See if the face is a match for the known face(s)
        matches = face_recognition.compare_faces(faces_encoded, face_encoding)
        name = "Unknown"

        # use the known face with the smallest distance to the new face
        face_distances = face_recognition.face_distance(faces_encoded, face_encoding)
        best_match_index = numpy.argmin(face_distances)
        if matches[best_match_index]:
            name = known_face_names[best_match_index]

        face_names.append(name)
        if name == str(user):
            is_found = True

    return is_found


def is_face_available(user):

    try:
        face = face_recognition.load_image_file("./media/Faces/" + str(user) + ".jpg")
    except IOError:
        return False

    if face is None:
        return False

    encoding = face_recognition.face_encodings(face)

    if len(encoding) == 0:
        print("some shit\n")
        return False

    return True


def delete_face(user):
    try:
        face = face_recognition.load_image_file("./media/Faces/" + str(user) + ".jpg")
    except IOError:
        return True

    if face is None:
        return True

    os.remove("./media/Faces/" + str(user) + ".jpg")
    return True
